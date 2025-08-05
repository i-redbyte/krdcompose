package ru.redbyte.krdcompose.screens.pythagoras

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.PythagorasTree

@Composable
fun PythagorasTreeScreen() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            PythagorasTree(
                levels = 9,
                colors = listOf(
                    Color(0xFF1B5E20),
                    Color(0xFF388E3C),
                    Color(0xFF4CAF50),
                    Color(0xFFC8E6C9)
                ),
                startAngle = 45f,
                angleRange = 0f..120f,
                durationMillis = 5500,
                rootRotation = 180f
            )
        }
    }
}