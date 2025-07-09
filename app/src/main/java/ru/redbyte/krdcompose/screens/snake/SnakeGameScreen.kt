package ru.redbyte.krdcompose.screens.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.redbyte.krdcompose.ui.games.snake.SnakeGame

@Composable
fun SnakeGameScreen() {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SnakeGame()
    }

}