package ru.redbyte.krdcompose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerDialog(
    initial: Color,
    onPick: (Color) -> Unit,
    onClose: () -> Unit
) {
    var r by rememberSaveable { mutableIntStateOf((initial.red * 255).roundToInt()) }
    var g by rememberSaveable { mutableIntStateOf((initial.green * 255).roundToInt()) }
    var b by rememberSaveable { mutableIntStateOf((initial.blue * 255).roundToInt()) }
    var a by rememberSaveable { mutableIntStateOf((initial.alpha * 255).roundToInt()) }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Выбор цвета") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(Color(r, g, b, a))
                        .border(1.dp, Color.Gray, CircleShape)
                )
                ChannelSlider("R", r) { r = it }
                ChannelSlider("G", g) { g = it }
                ChannelSlider("B", b) { b = it }
                ChannelSlider("A", a) { a = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onPick(Color(r, g, b, a))
            }) { Text("ОК") }
        },
        dismissButton = {
            TextButton(onClick = { onClose() }) { Text("Отмена") }
        }
    )
}

@Composable
fun ChannelSlider(name: String, value: Int, onValue: (Int) -> Unit) {
    Column {
        Text("$name: $value")
        Slider(
            value = value.toFloat(),
            onValueChange = { onValue(it.roundToInt().coerceIn(0, 255)) },
            valueRange = 0f..255f,
            steps = 254
        )
    }
}
