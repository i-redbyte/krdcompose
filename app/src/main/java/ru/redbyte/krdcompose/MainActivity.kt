package ru.redbyte.krdcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.redbyte.krdcompose.screens.sapper.SapperGameScreen
import ru.redbyte.krdcompose.screens.dataSlider.DataSliderScreen
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
            composable("dataSliderScreen") { DataSliderScreen() }
            composable("sapperScreen") { SapperGameScreen() }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Открыть экран:")
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navController.navigate("dataSliderScreen")
            }) {
            Text("Data Slider")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("sapperScreen") }) {
            Text("Сапер")
        }
    }
}

