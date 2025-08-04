package ru.redbyte.krdcompose.screens.hanoi

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ru.redbyte.krdcompose.games.hanoiTowers.TowerOfHanoiGame

@Composable
fun HanoiGameScreen() {
    val ctx = LocalContext.current
    var autoTrigger by remember { mutableIntStateOf(0) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.LightGray)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { autoTrigger++ }) { Text("Автоигра") }

            TowerOfHanoiGame(
                rods = 4,
                rings = 6,
                ringColors = listOf(
                    Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFFFF176),
                    Color(0xFF81C784), Color(0xFF673AB7), Color(0xFF4CAF50),
                    Color(0xFFEB04F3), Color(0xFFA40431), Color(0xFF002342), Color.Black
                ),
                onVictory = { moves ->
                    Toast.makeText(ctx, "Победа за $moves ходов.", Toast.LENGTH_LONG).show()
                },
                autoPlayTrigger = autoTrigger,
                onAutoPlayFinish = {
                    Toast.makeText(ctx, "Авто-игра завершена!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}