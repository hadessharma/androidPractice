package com.example.test_app_ui

import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test_app_ui.ui.theme.TestappuiTheme

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestappuiTheme {
                Surface(
                    Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Greeting(
                            name = "Android", modifier = Modifier
                        )
                        HomeButtons()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeButtons() {
    val context = LocalContext.current

    // Manage permission request
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                Log.d("Permission", "Camera permission granted")
            } else {
                Log.d("Permission", "Camera permission denied")
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp) // Padding around the Row
        ) {
            Button(
                onClick = {
                    // Request permission if not already granted
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    // Open camera with flash
                    openCameraWithFlash(context)
                },
                modifier = Modifier
                    .padding(8.dp) // Padding around each button
                    .weight(1f), // Distribute space evenly
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Heart")
            }

            Button(
                onClick = {
                    // Handle button click
                },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text("Respiration")
            }
        }
    }
}

fun openCameraWithFlash(context: Context) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        val preview = CameraXPreview.Builder().build()
        val previewView = PreviewView(context)

        // Set up the preview view
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Bind camera lifecycle
            val camera: Camera = cameraProvider.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                preview
            )

            // Turn on the flash
            val cameraControl: CameraControl = camera.cameraControl
            cameraControl.enableTorch(true) // Flash on

        } catch (e: Exception) {
            Log.e("CameraX", "Failed to bind camera use cases", e)
        }

    }, ContextCompat.getMainExecutor(context))
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestappuiTheme {
        Greeting("Android")
    }
}