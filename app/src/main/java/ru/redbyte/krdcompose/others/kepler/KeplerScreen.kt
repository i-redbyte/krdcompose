package ru.redbyte.krdcompose.others.kepler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.*






@Composable
fun LabeledNumberField(label: String, value: Double, onChange: (Double) -> Unit) {
    var txt by rememberSaveable { mutableStateOf(value.toString()) }
    Column {
        Text(label)
        OutlinedTextField(
            value = txt,
            onValueChange = {
                txt = it
                it.toDoubleOrNull()?.let(onChange)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
) {
    Column {
        Text("$label: ${"%.2f".format(value)}")
        Slider(value = value, onValueChange = onChange, valueRange = valueRange, steps = steps)
    }
}

@Composable
fun <T : Enum<T>> DropdownEnum(current: T, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(current.name) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            current.javaClass.enumConstants?.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.name) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorRow(title: String, color: Color, onChange: (Color) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.White, CircleShape)
            )
            OutlinedButton(onClick = { showPicker = true }) {
                Text("Выбрать")
            }
        }
    }

    if (showPicker) {
        ColorPickerDialog(
            initial = color,
            onPick = {
                onChange(it)
                showPicker = false
            },
            onClose = { showPicker = false }
        )
    }
}

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

