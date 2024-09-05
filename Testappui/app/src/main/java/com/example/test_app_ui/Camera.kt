package com.example.test_app_ui

import android.content.Context
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreviewView(context: Context) {
    // Create the PreviewView inside a Box
    Box(modifier = Modifier.size(200.dp, 200.dp).padding(6.dp)) {
        // Use AndroidView to integrate PreviewView into Jetpack Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // Create the PreviewView
                PreviewView(ctx).apply {
                    // Set up camera preview or other configurations if needed
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                // Bind the camera provider to the lifecycle or update the preview
                startCameraPreview(previewView, context)
            }
        )
    }
}

fun startCameraPreview(previewView: PreviewView, context: Context) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build()

        // Bind the PreviewView to the camera
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}
