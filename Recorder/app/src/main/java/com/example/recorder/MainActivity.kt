package com.example.recorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recorder.ui.theme.RecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }

    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("heart") { HeartScreen() }
            composable("breathe") { BreatheScreen() }
        }
    }
}


@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding
        verticalArrangement = Arrangement.Center, // Center the buttons vertically
        horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
    ) {
        Button(onClick = { navController.navigate("heart") }) {
            Text(text = "Go to Heart Screen")
        }
        Spacer(modifier = Modifier.height(16.dp)) // Add space between buttons
        Button(onClick = { navController.navigate("breathe") }) {
            Text(text = "Go to Breathe Screen")
        }
    }
}


@Composable
fun HeartScreen() {
    Text(text = "heart")
}

@Composable
fun BreatheScreen() {
    Text(text = "breathe")
}
