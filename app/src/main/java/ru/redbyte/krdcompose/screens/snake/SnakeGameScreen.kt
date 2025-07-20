package ru.redbyte.krdcompose.screens.snake

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.redbyte.krdcompose.games.snake.RenderMode
import ru.redbyte.krdcompose.games.snake.SnakeGame

@Composable
fun SnakeGameScreen() {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        SnakeGame(
            headColor = Color(0xFFB21AF5),
            foodColor = Color(0xFF9EF502),
            tailColor = Color(0xFFFFEB3B),
            livesCount = 5,
            mode = RenderMode.IMAGE,
            emojiFood = "\uD83C\uDF7A",
            emojiHead = "\uD83D\uDE0D"
        )
    }

}