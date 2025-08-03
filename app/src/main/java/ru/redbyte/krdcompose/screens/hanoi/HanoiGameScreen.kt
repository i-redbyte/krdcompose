package ru.redbyte.krdcompose.screens.hanoi

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ru.redbyte.krdcompose.games.hanoiTowers.TowerOfHanoiGame

@Composable
fun HanoiGameScreen() {
    val ctx = LocalContext.current
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TowerOfHanoiGame(
                rods = 3,
                rings = 6,
                ringColors = listOf(
                    Color(0xFFE57373),
                    Color(0xFF64B5F6),
                    Color(0xFFFFF176),
                    Color(0xFF81C784),
                    Color(0xFF673AB7),
                    Color(0xFF009688),
                    Color(0xFFEB04F3),
                    Color(0xFFA40431),
                    Color(0xFF002342),
                    Color(0xFF050000),
                ),
                onVictory = { moves ->
                    Toast
                        .makeText(
                            ctx,
                            "Победа за $moves ходов.",
                            Toast.LENGTH_LONG
                        ).show()
                }
            )
        }
    }
}
