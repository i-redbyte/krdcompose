package ru.redbyte.krdcompose.screens.race

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.redbyte.krdcompose.games.race.RacingGame

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun RaceScreen() {
    val ctx = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top + WindowInsetsSides.Bottom)
                    .asPaddingValues()
            )
    ) { innerPadding ->
        RacingGame(
            onGameOver = { time, score ->
                Toast.makeText(
                    ctx,
                    "Time: $time sec, Score: $score",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}