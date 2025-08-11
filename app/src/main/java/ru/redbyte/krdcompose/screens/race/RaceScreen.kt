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


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RaceScreen() {
    val ctx = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        RacingGame(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onExit = { (ctx as? android.app.Activity)?.finish() }
        )
    }
}