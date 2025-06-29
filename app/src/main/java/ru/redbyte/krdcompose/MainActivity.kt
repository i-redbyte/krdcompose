package ru.redbyte.krdcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.redbyte.krdcompose.screens.DataSliderScreen
import ru.redbyte.krdcompose.ui.theme.KrdcomposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KrdcomposeTheme {
                InitNavigation()
            }
        }
    }
    @Composable
    private fun InitNavigation() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "mainScreen"
        ) {
            composable("mainScreen") { MainScreen(navController) }
            composable("dataSliderScreen") { DataSliderScreen(/*navController*/) }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Открыть экран:")
        Button(onClick = {
            navController.navigate("dataSliderScreen")
        }) {
            Text("Data Slider")
        }
    }
}

