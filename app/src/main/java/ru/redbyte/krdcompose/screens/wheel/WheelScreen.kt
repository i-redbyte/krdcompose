package ru.redbyte.krdcompose.screens.wheel

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.FortuneWheel
import ru.redbyte.krdcompose.ui.components.WheelItem

@Composable
fun WheelScreen() {
    val items = remember {
        listOf(
            WheelItem("50"),
            WheelItem("100"),
            WheelItem("150"),
            WheelItem("200"),
            WheelItem("250"),
            WheelItem("300"),
            WheelItem("350"),
            WheelItem("400"),
            WheelItem("450"),
            WheelItem("500"),
            WheelItem("550"),
            WheelItem("1000"),
            WheelItem("1500"),
            WheelItem("2000"),
            WheelItem("3000"),
            WheelItem("4000"),
            WheelItem("5000"),
            WheelItem("C++"),
        )
    }
    var selected by remember { mutableStateOf<WheelItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FortuneWheel(
            items = items,
            modifier = Modifier.fillMaxWidth(0.9f),
            onItemSelected = {
                Log.d("_debug", "WheelScreen: $it")
                selected = it
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        selected?.let {
            Text(text = "Выпало: ${it.text}", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun WheelDemoPreview() {
    MaterialTheme {
        WheelScreen()
    }
}