@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.cameraxguide

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plcoding.cameraxguide.ui.theme.CameraXGuideTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private var recording: Recording? = null
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private val orientationValues = mutableListOf<FloatArray>()
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
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
                        )
                    }
                }

                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomSheetContent(
                            bitmaps = bitmaps, modifier = Modifier.fillMaxWidth()
                        )
                    }) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier
                                .align(Alignment.TopCenter) // Align at the top center
                                .padding(16.dp) // Padding around the preview
                                .size(200.dp) // Set a fixed size for the preview

                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            // Symptoms and Upload Signs Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.width(150.dp),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    Text("Upload Signs")
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 40.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.width(150.dp),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    Text("Symptoms")
                                }
                            }
                            // Heart Rate and Respiration Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(onClick = {
                                    recordVideo(controller)
                                }) {
                                    Text("Heart Rate")
                                }
                                Button(onClick = {}) {
                                    Text("Respiration")
                                }
                            }
                        }

                    }
                }

                startOrientationDataCollection()
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

    // Starts collecting orientation data
    private fun startOrientationDataCollection() {
        if (!isCollectingData) {
            rotationVectorSensor?.let { sensor ->
                sensorManager.registerListener(
                    this, sensor, SensorManager.SENSOR_DELAY_UI
                )
            }
            isCollectingData = true

            // Stop collecting data after 45 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                stopCollectingData()
            }, 45000)
        }
    }

    // This method is called when sensor data changes
    fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, it.values)

                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // Z axis
                val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()   // X axis
                val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()    // Y axis

                // Log the data
                Log.d(TAG, "Orientation: Z: $azimuth, X: $pitch, Y: $roll")

                // Add values to the list
                orientationValues.add(floatArrayOf(azimuth, pitch, roll))
            }
        }
    }

    // This method is called after 45 seconds to stop data collection
    private fun stopCollectingData() {
        sensorManager.unregisterListener(this, rotationVectorSensor)
        isCollectingData = false
        Log.d(TAG, "Data collection complete")

        // Log the collected data
        for (orientation in orientationValues) {
            Log.d(TAG, "Collected Orientation: Z: ${orientation[0]}, X: ${orientation[1]}, Y: ${orientation[2]}")
        }
    }

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
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

