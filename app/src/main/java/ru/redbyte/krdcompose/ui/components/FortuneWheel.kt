package ru.redbyte.krdcompose.ui.components

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class WheelItem(val text: String)

@Composable
fun FortuneWheel(
    items: List<WheelItem>,
    modifier: Modifier = Modifier,
    onItemSelected: (WheelItem) -> Unit
) {
    require(items.size >= 2)

    var isSpinning by rememberSaveable { mutableStateOf(false) }
    var currentAngle by rememberSaveable { mutableFloatStateOf(0f) }
    var startAngle by rememberSaveable { mutableFloatStateOf(0f) }
    var targetAngle by rememberSaveable { mutableFloatStateOf(0f) }
    var startTime by rememberSaveable { mutableLongStateOf(0L) }
    var endTime by rememberSaveable { mutableLongStateOf(0L) }

    fun startSpin() {
        if (isSpinning) return
        val seg = 360f / items.size
        val chosen = items.indices.random()
        val fullRot = (3..5).random()
        val duration = (3000..6000).random()
        startAngle = currentAngle

        val center = 90f + chosen * seg
        val now = ((currentAngle % 360f) + 360f) % 360f
        var delta = 450f - center - now
        delta = ((delta % 360f) + 360f) % 360f

        targetAngle = currentAngle + fullRot * 360f + delta
        startTime = SystemClock.uptimeMillis()
        endTime = startTime + duration
        isSpinning = true
    }

    LaunchedEffect(isSpinning) {
        if (!isSpinning) return@LaunchedEffect
        while (isSpinning) {
            val nowMs = withFrameNanos { it } / 1_000_000L
            val total = (endTime - startTime).toFloat()
            val passed = (nowMs - startTime).coerceAtLeast(0L).toFloat()
            val frac = (passed / total).coerceIn(0f, 1f)
            val eased = 2f * frac - frac * frac
            currentAngle = startAngle + (targetAngle - startAngle) * eased
            if (frac >= 1f) {
                currentAngle = ((targetAngle % 360f) + 360f) % 360f
                val seg = 360f / items.size
                val normalized = ((currentAngle % 360f) + 360f) % 360f
                val index = ((360f - normalized) / seg).roundToInt() % items.size
                onItemSelected(items[index])
                isSpinning = false
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = !isSpinning,
                interactionSource = null,
                indication = null
            ) { startSpin() }
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = currentAngle }
        ) {
            val r = min(size.width, size.height) / 2
            val seg = 360f / items.size
            val startOff = 90f - seg / 2
            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = r * 0.1f
            }
            for (i in items.indices) {
                val s = startOff + i * seg
                val col = if (i % 2 == 0) Color.Black else Color.White
                drawArc(
                    color = col,
                    startAngle = s,
                    sweepAngle = seg,
                    useCenter = true,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2, r * 2)
                )
                textPaint.color =
                    if (col == Color.Black) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                val a = (s + seg / 2) * (PI / 180).toFloat()
                val tx = center.x + cos(a) * r * 0.7f
                val ty = center.y + sin(a) * r * 0.7f
                drawIntoCanvas {
                    it.nativeCanvas.save()
                    it.nativeCanvas.rotate(s + seg / 2 + 90f, tx, ty)
                    it.nativeCanvas.drawText(items[i].text, tx, ty, textPaint)
                    it.nativeCanvas.restore()
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val r = min(size.width, size.height) / 2
            val b = r * 0.1f
            val arrow = Path().apply {
                moveTo(center.x, center.y)
                lineTo(center.x - b / 2, center.y)
                lineTo(center.x, center.y + r * 0.95f)
                lineTo(center.x + b / 2, center.y)
                close()
            }
            drawPath(arrow, Color.Red)
        }
    }
}
