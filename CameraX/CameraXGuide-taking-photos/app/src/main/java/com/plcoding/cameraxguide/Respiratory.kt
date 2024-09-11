package com.plcoding.cameraxguide


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class Respiratory {

    @Composable
    fun OrientationDataCollectionScreen(onStartButtonClick: () -> Unit) {
        // Simple UI with a button to start data collection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { onStartButtonClick() }) {
                Text("Start Orientation Data Collection")
            }
        }
    }
}
