@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.cameraxguide

import SymptomsButtonWithDropdown
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plcoding.cameraxguide.ui.theme.CameraXGuideTheme
import kotlinx.coroutines.launch
import respiratoryRateCalculator
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MainActivity : ComponentActivity(), SensorEventListener {

    private var recording: Recording? = null
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
//    private val orientationValues = mutableListOf<FloatArray>()

    private var heartRate by mutableStateOf("N/A")
    private var respiratoryRate by mutableStateOf("N/A")

    private val azimuthValues = mutableListOf<Float>()
    private val pitchValues = mutableListOf<Float>()
    private val rollValues = mutableListOf<Float>()

    private val TAG = "OrientationData"
    private var isCollectingData by mutableStateOf(false)


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }
        setContent {
            CameraXGuideTheme {

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                        )
                    }
                }

                val viewModel = viewModel<MainViewModel>()


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp) // Adjusted padding for the entire box
                ) {
                    CameraPreview(
                        controller = controller,
                        modifier = Modifier
                            .align(Alignment.TopCenter) // Align at the top center
                            .padding(top = 20.dp) // Reduced top padding around the preview
                            .size(180.dp) // Increased size for better visibility
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp) // Adjusted bottom padding
                    ) {
                        // Symptoms and Upload Signs Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 70.dp), // Adjusted padding between rows
                            horizontalArrangement = Arrangement.SpaceEvenly // Even space around buttons
                        ) {
                            Button(
                                onClick = {},
                                modifier = Modifier.width(160.dp),
                                contentPadding = PaddingValues(12.dp) // Increased padding for better touch area
                            ) {
                                Text("Upload Signs")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 50.dp), // Adjusted padding between rows
                            horizontalArrangement = Arrangement.SpaceEvenly // Even space around buttons
                        ) {
                            SymptomsButtonWithDropdown()
                        }
                        // Heart Rate and Respiration Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "$heartRate",
                                modifier = Modifier.padding(top = 2.dp, start = 10.dp),
                                fontSize = 15.sp,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$respiratoryRate",
                                modifier = Modifier.padding(top = 2.dp, start = 150.dp),
                                fontSize = 15.sp,
                                style = MaterialTheme.typography.titleMedium
                            )

                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            Button(
                                onClick = { recordVideo(controller) },
                                modifier = Modifier
                                    .weight(1f) // Flexible size
                                    .padding(end = 8.dp) // Spacing between buttons
                            ) {
                                Text("Heart Rate")
                            }

                            Button(
                                onClick = { startOrientationDataCollection() },
                                modifier = Modifier
                                    .weight(1f) // Flexible size
                                    .padding(start = 8.dp)
                            ) {
                                Text("Respiration")
                            }
                        }
                    }
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun recordVideo(controller: LifecycleCameraController) {
        if (recording != null) {
            recording?.stop()
            recording = null
            controller.cameraControl?.enableTorch(false)
            return
        }

        if (!hasRequiredPermissions()) {
            return
        }

        controller.cameraControl?.enableTorch(true)
        val outputFile = File(filesDir, "my-recording.mp4")

        recording = controller.startRecording(
            FileOutputOptions.Builder(outputFile).build(),
            AudioConfig.AUDIO_DISABLED,
            ContextCompat.getMainExecutor(applicationContext)
        ) { event ->
            when (event) {
                is VideoRecordEvent.Finalize -> {
                    controller.cameraControl?.enableTorch(false)
                    if (event.hasError()) {
                        recording?.close()
                        recording = null

                        Toast.makeText(
                            applicationContext,
                            "Video capture failed: ${event.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext, "Video capture succeeded", Toast.LENGTH_LONG
                        ).show()

                        // Call heartRateCalculator after video capture succeeds
                        lifecycleScope.launch {
                            try {
                                val rate =
                                    heartRateCalculator(Uri.fromFile(outputFile), contentResolver)
                                heartRate = rate.toString()
                                // Use the result (rate) here
                                Log.d("HeartRate", "Calculated heart rate: $rate")
                            } catch (e: Exception) {
                                Log.e("HeartRate", "Error calculating heart rate", e)
                            }
                        }
                    }
                }
            }
        }

        // Stop the recording after 10 seconds (adjust as needed)
        Handler(Looper.getMainLooper()).postDelayed({
            recording?.stop()
            recording = null
            controller.cameraControl?.enableTorch(false) // Turn off the torch after stopping
        }, 45000)
    }

    // Implementing onSensorChanged() from SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // Convert radians to degrees for orientation
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // Z axis
                val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()   // X axis
                val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()    // Y axis

                // FOR REAL RESPIRATION
                // Log the data and store it
                Log.d(TAG, "Orientation: Z (Azimuth): $azimuth, X (Pitch): $pitch, Y (Roll): $roll")

                // Add the values to the list
                azimuthValues.add(azimuth)
                pitchValues.add(pitch)
                rollValues.add(roll)
            }
        }
    }

    // Implementing onAccuracyChanged() from SensorEventListener
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    // Function to start collecting data
    private fun startOrientationDataCollection() {
        if (!isCollectingData) {
            rotationVectorSensor?.let { sensor ->
                sensorManager.registerListener(
                    this, sensor, SensorManager.SENSOR_DELAY_UI
                ) // 'this' is now the listener
            }
            isCollectingData = true

            // Stop collecting data after 45 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                stopCollectingData()
            }, 45000)
        }
    }

    // Function to stop collecting data
    private fun stopCollectingData() {
        sensorManager.unregisterListener(this, rotationVectorSensor)
        isCollectingData = false
        Log.d(TAG, "Data collection complete")

        // FOR CSV
//        val fileNames = listOf(
//            "CSVBreatheX.csv",
//            "CSVBreatheY.csv",
//            "CSVBreatheZ.csv"
//        )
//        val listsOfFloats = mutableListOf<MutableList<Float>>()
//
//        for (fileName in fileNames) {
//            val floatList = readCsvFileFromAssets(context = applicationContext, fileName)
//            listsOfFloats.add(floatList)
//        }
//        val azimuthValues = listsOfFloats[0]
//        val pitchValues = listsOfFloats[1]
//        val rollValues = listsOfFloats[2]

//        Log.d(TAG, "Collected azimuth values: $azimuthValues")
        respiratoryRate =
            respiratoryRateCalculator(azimuthValues, pitchValues, rollValues).toString()
        azimuthValues.clear()
        pitchValues.clear()
        rollValues.clear()
        Log.d(TAG, "Calculated respiratory rate: $respiratoryRate")
    }

    private fun readCsvFileFromAssets(context: Context, fileName: String): MutableList<Float> {
        val mutableList = mutableListOf<Float>()

        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                val values = line.split(",")
                values.forEach { value ->
                    mutableList.add(value.trim().toFloatOrNull() ?: 0.0f)
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mutableList
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}

