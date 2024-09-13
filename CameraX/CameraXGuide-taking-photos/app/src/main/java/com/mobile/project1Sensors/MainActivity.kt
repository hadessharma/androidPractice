@file:OptIn(ExperimentalMaterial3Api::class)

package com.mobile.project1Sensors

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobile.project1Sensors.theme.CameraXGuideTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


class MainActivity : ComponentActivity(), SensorEventListener {


    private var recording: Recording? = null
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null


    private var heartRate by mutableStateOf("N/A")
    private var respiratoryRate by mutableStateOf("N/A")

    private val azimuthValues = mutableListOf<Float>()
    private val pitchValues = mutableListOf<Float>()
    private val rollValues = mutableListOf<Float>()

    private val TAG = "OrientationData"
    private var isCollectingData by mutableStateOf(false)

    private var isCameraPreviewEnabled by mutableStateOf(true)
    private fun toggleCameraPreview() {
        isCameraPreviewEnabled = !isCameraPreviewEnabled
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            CameraXGuideTheme {
                // Create NavController
                val navController = rememberNavController()


                NavHost(navController = navController, startDestination = "main") {
                    // Main Screen
                    composable("main") {
                        MainScreen(navController = navController)
                    }
                    // Symptoms Screen
                    composable("symptoms") {
                        SymptomsButtonWithDropdown(
                            application = applicationContext,
                            heartRate = heartRate,
                            respiratoryRate = respiratoryRate,
                            navController = navController
                        )
                    }
                }


            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    @Composable
    fun MainScreen(navController: NavHostController) {

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)


        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        val controller = remember {
            LifecycleCameraController(applicationContext).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                )
            }
        }

        // Use Box to layer and position elements properly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp) // General padding around the box
        ) {
            // Camera preview at the top center
            if (isCameraPreviewEnabled) {
                CameraPreview(
                    controller = controller,
                    modifier = Modifier
                        .align(Alignment.TopCenter) // Align at the top center
                        .padding(top = 40.dp) // Padding around the preview
                        .size(150.dp) // Set a fixed size for the preview
                )
            }

            IconButton(
                onClick = { toggleCameraPreview() },
                modifier = Modifier
                    .align(Alignment.TopCenter) // Align below the camera preview
                    .padding(top = 250.dp) // Adjust padding to position the button below the preview
                    .size(100.dp) // Size of the button
            ) {
                Icon(
                    imageVector = if (isCameraPreviewEnabled) Icons.Filled.Camera else Icons.Filled.CameraAlt,
                    contentDescription = if (isCameraPreviewEnabled) "Turn Off Camera" else "Turn On Camera",
                    modifier = Modifier.size(50.dp) // Icon size
                )
            }

            // Buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter) // Align the buttons at the bottom
                    .padding(16.dp)
            ) {


                // Labels for Heart Rate and Respiratory Rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "Heart Rate: $heartRate",
                        modifier = Modifier.padding(vertical = 2.dp),
                        fontSize = 15.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Respiratory Rate: $respiratoryRate",
                        modifier = Modifier.padding(vertical = 2.dp),
                        fontSize = 15.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Heart Rate and Respiration Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            recordVideo(controller)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp) // Spacing between buttons
                    ) {
                        Text("Heart Rate")
                    }

                    Button(
                        onClick = { startOrientationDataCollection() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Respiration")
                    }
                }
                // Symptoms and Upload Signs Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(
                        onClick = {
                            // Navigate to Symptoms screen when Upload Signs button is clicked
                            navController.navigate("symptoms")
                        },
                        modifier = Modifier.width(150.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload, // Use an appropriate icon
                                contentDescription = "Upload",
                                modifier = Modifier.size(24.dp) // Icon size
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                            Text(
                                text = "Upload Signs", fontSize = 16.sp
                            )
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
        val outputFile = File(filesDir, "hearRate.mp4")

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
                        toggleCameraPreview()
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

        // Stop the recording after 45 seconds (adjust as needed)
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

