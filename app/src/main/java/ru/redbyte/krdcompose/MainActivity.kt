package ru.redbyte.krdcompose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import ru.redbyte.krdcompose.screens.chess.ChessScreen
import ru.redbyte.krdcompose.screens.sapper.SapperGameScreen
import ru.redbyte.krdcompose.screens.dataSlider.DataSliderScreen
import ru.redbyte.krdcompose.screens.hanoi.HanoiGameScreen
import ru.redbyte.krdcompose.screens.kepler.KeplerScreen
import ru.redbyte.krdcompose.screens.numberSystem.NumberSystemScreen
import ru.redbyte.krdcompose.screens.pascalTriangle.PascalTriangleScreen
import ru.redbyte.krdcompose.screens.pythagoras.PythagorasTreeScreen
import ru.redbyte.krdcompose.screens.race.RaceScreen
import ru.redbyte.krdcompose.screens.snake.SnakeGameScreen
import ru.redbyte.krdcompose.screens.sudoku.SudokuApp
import ru.redbyte.krdcompose.screens.wheel.WheelScreen
import ru.redbyte.krdcompose.ui.theme.KrdcomposeTheme

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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
            composable("pascalTriangleScreen") { PascalTriangleScreen() }
            composable("numberSystemScreen") { NumberSystemScreen() }
            composable("snakeGameScreen") { SnakeGameScreen() }
            composable("chessScreen") { ChessScreen() }
            composable("wheelScreen") { WheelScreen() }
            composable("hanoiGameScreen") { HanoiGameScreen() }
            composable("pythagorasTreeScreen") { PythagorasTreeScreen() }
            composable("raceScreen") { RaceScreen() }
            composable("sudokuGameScreen") { SudokuApp() }
            composable("keplerOrbitScreen") { KeplerScreen() }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
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
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("pascalTriangleScreen") }) {
            Text("Треугольник Паскаля")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("numberSystemScreen") }) {
            Text("Системы счисления")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("snakeGameScreen") }) {
            Text("Змейка")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("chessScreen") }) {
            Text("Шахматы")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("wheelScreen") }) {
            Text("Колесо фортуны")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("hanoiGameScreen") }) {
            Text("Ханойские башни")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("pythagorasTreeScreen") }) {
            Text("Дерево Пифагора")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("raceScreen") }) {
            Text("Гонки")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("sudokuGameScreen") }) {
            Text("Судоку")
        }

        Spacer(Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("keplerOrbitScreen") }) {
            Text("Моделирование орбиты Кеплера")
        }

    }
}

