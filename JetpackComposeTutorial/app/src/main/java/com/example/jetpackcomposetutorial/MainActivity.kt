package com.example.jetpackcomposetutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackcomposetutorial.ui.theme.JetpackComposeTutorialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeTutorialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().padding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Banner()
                }
            }
        }
    }
}

@Composable
fun TutorialTex(heading: String, para2: String, para3: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = heading,
            fontSize = 24.sp,
            modifier = modifier.padding(16.dp)
        )
        Text(
            text = para2,
            fontSize = 24.sp,
            textAlign = TextAlign.Justify,
            modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        Text(
            text = para3,
            fontSize = 24.sp,
            textAlign = TextAlign.Justify,
            modifier = modifier.padding(start = 16.dp, end = 16.dp),
            )
    }
}

@Composable
fun Banner(modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.bg_compose_background)
    Column(
        modifier = modifier
    ) {
        Image(
            painter = image,
            contentDescription = null,
//            contentScale = ContentScale.Crop,
        )
        TutorialTex(
            heading = stringResource(R.string.heading_1),
            para2 = stringResource(R.string.para_2),
            para3 = stringResource(R.string.para_3)
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetpackComposeTutorialTheme {
        Banner()
    }
}