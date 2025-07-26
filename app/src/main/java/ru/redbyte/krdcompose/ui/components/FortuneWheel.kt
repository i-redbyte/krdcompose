package ru.redbyte.krdcompose.ui.components

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class WheelItem(val text: String)

val easingMap: LinkedHashMap<String, (Float) -> Float> = linkedMapOf(
    "Sine‑out" to { t -> sin(t * PI.toFloat() / 2f) },
    "Cubic‑out" to { t -> 1f - (1f - t).pow(3) },
    "Quint‑out" to { t -> 1f - (1f - t).pow(5) },
    "Quad‑out" to { t -> 1f - (1f - t).pow(2) },
    "Expo‑out" to { t -> if (t == 1f) 1f else 1f - 2f.pow(-10f * t) },
    "Circ‑out" to { t -> sqrt(1f - (t - 1f).pow(2)) },
    "Back‑out" to { t ->
        val s = 1.70158f;
        val p = t - 1f; 1f + (s + 1f) * p.pow(3) + s * p.pow(2)
    },
    "Elastic‑out" to { t: Float ->
        when (t) {
            0f -> 0f
            1f -> 1f
            else -> {
                val p = 0.3f
                (2f.pow(-10f * t) *
                        sin((t - p / 4f) * (2f * PI) / p).toFloat() + 1f)
            }
        }
    },
    "Bounce‑out" to { t ->
        val n1 = 7.5625f;
        val d1 = 2.75f
        when {
            t < 1f / d1 -> n1 * t * t
            t < 2f / d1 -> {
                val p = t - 1.5f / d1; n1 * p * p + 0.75f
            }

            t < 2.5f / d1 -> {
                val p = t - 2.25f / d1; n1 * p * p + 0.9375f
            }

            else -> {
                val p = t - 2.625f / d1; n1 * p * p + 0.984375f
            }
        }
    },
    "Linear" to { t -> t },
    "Quart‑out" to { t -> 1f - (1f - t).pow(4) },
    "Sine‑inOut" to { t -> (-(cos(PI.toFloat() * t) - 1f) / 2f) },
    "Quad‑inOut" to { t -> if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).pow(2) / 2f },
    "Expo‑inOut" to { t ->
        when (t) {
            0f -> 0f
            1f -> 1f
            else -> if (t < 0.5f) 2f.pow(20f * t - 10f) / 2f
            else (2f - 2f.pow(-20f * t + 10f)) / 2f
        }
    },
    "Bounce‑inOut" to { t ->
        val bounceOut = easingMap["Bounce‑out"]!!
        if (t < 0.5f) (1f - bounceOut(1f - 2f * t)) / 2f else (1f + bounceOut(2f * t - 1f)) / 2f
    },
    "Quart‑in" to { t -> t.pow(4) },
    "Quint‑inOut" to { t -> if (t < 0.5f) 16f * t.pow(5) else 1f - (-2f * t + 2f).pow(5) / 2f },
    "Circ‑inOut" to { t ->
        if (t < 0.5f) (1f - sqrt(1f - (2f * t).pow(2))) / 2f else (sqrt(
            1f - (-2f * t + 2f).pow(
                2
            )
        ) + 1f) / 2f
    },
    "Back‑inOut" to { t ->
        val c1 = 1.70158f;
        val c2 = c1 * 1.525f
        if (t < 0.5f) ((2f * t).pow(2) * ((c2 + 1f) * 2f * t - c2)) / 2f
        else ((2f * t - 2f).pow(2) * ((c2 + 1f) * (t * 2f - 2f) + c2) + 2f) / 2f
    },
    "Elastic‑inOut" to { t ->
        when (t) {
            0f -> 0f
            1f -> 1f
            else -> {
                val c = (2f * PI).toFloat();
                val p = 0.45f
                if (t < 0.5f)
                    ((-2f).pow(20f * t - 10f) * sin((20f * t - 11.125f) * c / p)) / 2f
                else
                    (2f.pow(-20f * t + 10f) * sin((20f * t - 11.125f) * c / p)) / 2f + 1f
            }
        }
    }
)

@Composable
fun FortuneWheel(
    items: List<WheelItem>,
    modifier: Modifier = Modifier,
    evenSectorColor: Color = Color.Black,
    oddSectorColor: Color = Color.White,
    evenTextColor: Color? = null,
    oddTextColor: Color? = null,
    easing: (Float) -> Float,
    forcedWinnerIndex: Int? = null,
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
        val chosen = forcedWinnerIndex
            ?.takeIf { it in items.indices }
            ?: items.indices.random()
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
            val t = (passed / total).coerceIn(0f, 1f)
            val eased = easing(t)
            currentAngle = startAngle + (targetAngle - startAngle) * eased
            if (t >= 1f) {
                currentAngle = ((targetAngle % 360f) + 360f) % 360f
                val seg = 360f / items.size
                val norm = ((currentAngle % 360f) + 360f) % 360f
                val idx = ((360f - norm) / seg).roundToInt() % items.size
                onItemSelected(items[idx])
                isSpinning = false
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = !isSpinning,
                interactionSource = remember { MutableInteractionSource() },
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
            repeat(items.size) { i ->
                val s = startOff + i * seg
                val sectorColor = if (i % 2 == 0) evenSectorColor else oddSectorColor
                drawArc(
                    color = sectorColor,
                    startAngle = s,
                    sweepAngle = seg,
                    useCenter = true,
                    topLeft = Offset(center.x - r, center.y - r),
                    size = Size(r * 2, r * 2)
                )
                val txtColor = when {
                    i % 2 == 0 && evenTextColor != null -> evenTextColor
                    i % 2 == 1 && oddTextColor != null -> oddTextColor
                    else -> if (sectorColor.luminance() < 0.5f) Color.White else Color.Black
                }
                textPaint.color = txtColor.toArgb()
                val ang = (s + seg / 2) * (PI / 180).toFloat()
                val tx = center.x + cos(ang) * r * 0.7f
                val ty = center.y + sin(ang) * r * 0.7f
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
