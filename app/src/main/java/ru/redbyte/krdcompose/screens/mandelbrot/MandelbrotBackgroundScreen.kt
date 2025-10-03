package ru.redbyte.krdcompose.screens.mandelbrot

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.others.mandelbrot.MandelbrotBackground

@Composable
fun MandelbrotBackgroundScreen() {
    var maxIterations by rememberSaveable { mutableIntStateOf(300) }
    var centerX by rememberSaveable { mutableFloatStateOf(-0.5f) }
    var centerY by rememberSaveable { mutableFloatStateOf(0f) }
    var scale by rememberSaveable { mutableFloatStateOf(2.8f) } //
    var hueShift by rememberSaveable { mutableFloatStateOf(0.1f) } // 0..1
    var animationSpeed by rememberSaveable { mutableFloatStateOf(0.25f) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .onSizeChanged { containerSize = it }
            .pointerInput(containerSize) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val h = containerSize.height.coerceAtLeast(1)
                    centerX -= (pan.x / h) * scale
                    centerY -= (pan.y / h) * scale
                    scale = (scale / zoom).coerceIn(1e-4f, 10f)
                }
            }
    ) {
        MandelbrotBackground(
            modifier = Modifier.fillMaxSize(),
            maxIterations = maxIterations,
            centerX = centerX,
            centerY = centerY,
            scale = scale,
            animationSpeed = animationSpeed,
            hueShift = hueShift
        )

        ControlPanel(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            maxIterations = maxIterations,
            onIterationsChange = { maxIterations = it },
            scale = scale,
            onScaleChange = { scale = it },
            hueShift = hueShift,
            onHueShiftChange = { hueShift = it },
            animationSpeed = animationSpeed,
            onAnimationSpeedChange = { animationSpeed = it },
            onReset = {
                maxIterations = 300
                centerX = -0.5f
                centerY = 0f
                scale = 2.8f
                hueShift = 0.1f
                animationSpeed = 0.25f
            }
        )
    }
}

@Composable
private fun ControlPanel(
    modifier: Modifier = Modifier,
    maxIterations: Int,
    onIterationsChange: (Int) -> Unit,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    hueShift: Float,
    onHueShiftChange: (Float) -> Unit,
    animationSpeed: Float,
    onAnimationSpeedChange: (Float) -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = modifier.widthIn(max = 420.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Mandelbrot",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onReset) { Text("Сброс") }
            }

            LabeledSlider(
                label = "Итерации: $maxIterations",
                value = maxIterations.toFloat(),
                onValueChange = { onIterationsChange(it.toInt()) },
                valueRange = 50f..1024f,
            )

            LabeledSlider(
                label = "Зум (scale): ${"%.4f".format(scale)}",
                value = scale,
                onValueChange = { onScaleChange(it) },
                valueRange = 0.0005f..4f,
            )

            LabeledSlider(
                label = "Скорость анимации: ${"%.2f".format(animationSpeed)}",
                value = animationSpeed,
                onValueChange = onAnimationSpeedChange,
                valueRange = 0f..2f,
            )

            LabeledSlider(
                label = "Hue shift: ${"%.2f".format(hueShift)}",
                value = hueShift,
                onValueChange = onHueShiftChange,
                valueRange = 0f..1f,
            )

        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    Column {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MandelbrotBackgroundScreenPreview() {
    MaterialTheme { MandelbrotBackgroundScreen() }
}
