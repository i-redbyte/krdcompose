package ru.redbyte.krdcompose.others.crack

import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random
import ru.redbyte.krdcompose.R

/** Должно совпадать с MAX_SEEDS в AGSL (crack_shader.agsl). */
const val CRACK_MAX_SEEDS: Int = 16

data class CrackSeed(
    val x: Float,
    val y: Float,
    val power: Float,        // 0..1
    val branchBoost: Float,  // 0..6
    val salt: Float = Random.nextFloat()
)

/**
 * Трещины как GPU-оверлей (AGSL рисуется поверх контента без RenderEffect),
 * опционально — CPU-оверлей для отладки (safetyOverlay/debugSegments).
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.crackedEffect(
    seeds: List<CrackSeed>,
    thicknessDp: Dp = 3.dp,
    jitterPx: Float = 12f,
    haloStrength: Float = 0.45f,
    safetyOverlay: Boolean = false,
    debugSegments: Boolean = false
): Modifier = composed {
    val density = LocalDensity.current
    var layerSize by remember { mutableStateOf(IntSize.Zero) }

    // === GPU overlay shader (без content), именно crack_overlay.agsl ===
    val overlayShader: RuntimeShader? = rememberCrackShaderWithFallback(
        fullResId = R.raw.crack_shader,
        lightResId = R.raw.crack_shader_light
    )
    val active = seeds.takeLast(CRACK_MAX_SEEDS)
    val seedCount = active.size
    val seedPos = FloatArray(CRACK_MAX_SEEDS * 2)
    val seedPow = FloatArray(CRACK_MAX_SEEDS)
    val seedBoost = FloatArray(CRACK_MAX_SEEDS)
    val seedSalt = FloatArray(CRACK_MAX_SEEDS)
    active.forEachIndexed { i, s ->
        seedPos[i * 2] = s.x
        seedPos[i * 2 + 1] = s.y
        seedPow[i] = s.power.coerceIn(0f, 1f)
        seedBoost[i] = s.branchBoost.coerceIn(0f, 6f)
        seedSalt[i] = s.salt
    }

    val overlayCPU = remember(active, layerSize, jitterPx) {
        OverlayGeometry.build(
            active,
            layerSize.width.toFloat(),
            layerSize.height.toFloat(),
            jitterPx
        )
    }

    SideEffect {
        if (layerSize.width > 0 && layerSize.height > 0) {
            overlayShader?.setFloatUniform(
                "uResolution",
                layerSize.width.toFloat(),
                layerSize.height.toFloat()
            )

            val thicknessPx = with(density) {
                thicknessDp.toPx().coerceAtLeast(0.6f)
            }
            overlayShader?.setFloatUniform(
                "uThickness",
                thicknessPx
            )
            overlayShader?.setFloatUniform(
                "uJitter",
                jitterPx.coerceAtLeast(0f)
            )
            overlayShader?.setFloatUniform(
                "uHalo",
                haloStrength.coerceIn(0f, 1f)
            )

            val dark = Color(0xF0000000)
            val core = Color(0x55FFFFFF)
            val halo = Color(0x00000000)
            overlayShader?.setFloatUniform(
                "uDarkColor",
                dark.red,
                dark.green,
                dark.blue,
                dark.alpha
            )
            overlayShader?.setFloatUniform(
                "uCoreColor",
                core.red,
                core.green,
                core.blue,
                core.alpha
            )
            overlayShader?.setFloatUniform(
                "uHaloColor",
                halo.red,
                halo.green,
                halo.blue,
                halo.alpha
            )

            overlayShader?.setFloatUniform(
                "uSeedPos",
                seedPos
            )
            overlayShader?.setFloatUniform(
                "uSeedPow",
                seedPow
            )
            overlayShader?.setFloatUniform(
                "uSeedBoost",
                seedBoost
            )
            overlayShader?.setFloatUniform(
                "uSeedSalt",
                seedSalt
            )
            overlayShader?.setFloatUniform(
                "uSeedCount",
                seedCount.toFloat()
            )
        }
    }

    val fwPaint = remember { Paint() }

    this
        .onSizeChanged { sz -> if (layerSize != sz) layerSize = sz }
        .drawWithContent {
            drawContent()

            if (
                overlayShader != null &&
                seedCount > 0 &&
                layerSize.width > 0 &&
                layerSize.height > 0
            ) {
                drawIntoCanvas { canvas ->
                    fwPaint.shader = overlayShader
                    canvas.nativeCanvas.drawRect(
                        0f, 0f, size.width, size.height, fwPaint
                    )
                }
            }

            if ((safetyOverlay || debugSegments) && overlayCPU.count > 0) {
                val w = with(density) { thicknessDp.toPx() }
                drawOverlayLines(
                    overlayCPU,
                    stroke = max(1f, w * if (debugSegments) 1.3f else 1.0f),
                    strong = debugSegments
                )
            }
        }
}


private object OverlayGeometry {
    private const val MAX_SEG = 180

    data class Segments(
        val a: FloatArray,
        val b: FloatArray,
        val count: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Segments

            if (count != other.count) return false
            if (!a.contentEquals(other.a)) return false
            if (!b.contentEquals(other.b)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = count
            result = 31 * result + a.contentHashCode()
            result = 31 * result + b.contentHashCode()
            return result
        }
    }

    fun build(
        seeds: List<CrackSeed>,
        w: Float,
        h: Float,
        jitterPx: Float
    ): Segments {
        val a = FloatArray(MAX_SEG * 2)
        val b = FloatArray(MAX_SEG * 2)
        if (seeds.isEmpty()) return Segments(a, b, 0)

        var count = 0

        seeds.forEach { s ->
            if (count >= MAX_SEG) return@forEach
            val pow = s.power.coerceIn(0f, 1f)
            val boost = s.branchBoost.coerceIn(0f, 6f)
            val radius = lerp(36f, 280f, pow)
            val branches = (6 + (10 * (pow + 0.06f * boost)).roundToInt()).coerceIn(6, 16)

            val rnd = StableRand.from(s.x, s.y, s.salt)
            val base = rnd.nextFloat() * PI2
            for (i in 0 until branches) {
                if (count >= MAX_SEG) break
                var px = s.x;
                var py = s.y
                val jitterA = (rnd.nextFloat() - 0.5f) * 0.6f
                val ang = base + 2.3999631f * i + jitterA
                val cx = cos(ang);
                val sy = sin(ang)
                val pxn = -sy;
                val pyn = cx
                val segs = 5
                val ph1 = rnd.nextFloat() * PI2
                val ph2 = rnd.nextFloat() * PI2
                for (k in 1..segs) {
                    if (count >= MAX_SEG) break
                    val t = k / segs.toFloat()
                    val len = radius * (0.6f * t + 0.4f * t * t)
                    val wob =
                        (sin(10f * t + ph1) + 0.5f * sin(23f * t + ph2)) * (1f - 0.28f * t) * jitterPx
                    val cxp = s.x + cx * len + pxn * wob
                    val cyp = s.y + sy * len + pyn * wob
                    if (!outside(px, py, cxp, cyp, w, h)) {
                        val i0 = count * 2
                        a[i0] = px; a[i0 + 1] = py
                        b[i0] = cxp; b[i0 + 1] = cyp
                        count++
                    }
                    px = cxp; py = cyp
                }
            }

            val rings = (1 + (4 * pow)).roundToInt().coerceIn(1, 4)
            val ringRnd = StableRand.from(s.x * 0.5f, s.y * 0.5f, s.salt * 0.5f)
            val ringBase = ringRnd.nextFloat() * PI2
            for (r in 0 until rings) {
                if (count >= MAX_SEG) break
                val rr = (0.3f + 0.65f * ringRnd.nextFloat()) * radius
                val arcSeg = 4
                val span = (0.6f + 0.8f * ringRnd.nextFloat()) * (PI / 2f)
                val a0 = ringBase + ringRnd.nextFloat() * PI2
                var px = s.x + rr * cos(a0)
                var py = s.y + rr * sin(a0)
                for (k in 1..arcSeg) {
                    if (count >= MAX_SEG) break
                    val t = k / arcSeg.toFloat()
                    val a1 = a0 + span * t * if (ringRnd.nextFloat() > 0.5f) 1f else -1f
                    val wob =
                        (ringRnd.nextFloat() - 0.5f) * 0.6f * (1f - 0.4f * t) * (jitterPx * 0.6f)
                    val cxp = s.x + (rr + wob) * cos(a1)
                    val cyp = s.y + (rr + wob) * sin(a1)
                    if (!outside(px, py, cxp, cyp, w, h)) {
                        val i0 = count * 2
                        a[i0] = px; a[i0 + 1] = py
                        b[i0] = cxp; b[i0 + 1] = cyp
                        count++
                    }
                    px = cxp; py = cyp
                }
            }
        }
        return Segments(a, b, count)
    }

    private fun outside(ax: Float, ay: Float, bx: Float, by: Float, w: Float, h: Float): Boolean {
        val minx = min(ax, bx);
        val maxx = max(ax, bx)
        val miny = min(ay, by);
        val maxy = max(ay, by)
        return (maxx < -24f || maxy < -24f || minx > w + 24f || miny > h + 24f)
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private const val PI = Math.PI.toFloat()
    private const val PI2 = (Math.PI * 2.0).toFloat()

    private class StableRand private constructor(seed: Long) {
        private var state = seed
        fun nextFloat(): Float {
            var x = state
            x = x xor (x shl 13)
            x = x xor (x ushr 7)
            x = x xor (x shl 17)
            state = x
            val u = (x ushr 1) / (Long.MAX_VALUE.toFloat())
            return when {
                u >= 1f -> 0.999999f
                u < 0f -> 0f
                else -> u
            }
        }

        companion object {
            fun from(x: Float, y: Float, salt: Float): StableRand {
                val xi = java.lang.Float.floatToIntBits(x).toLong()
                val yi = java.lang.Float.floatToIntBits(y).toLong()
                val si = java.lang.Float.floatToIntBits(salt).toLong()
                var seed = xi xor (yi shl 21) xor (si shl 43)
                seed *= 0x9E3779B97F4A7C15UL.toLong()
                return StableRand(seed)
            }
        }
    }
}

private fun DrawScope.drawOverlayLines(
    geo: OverlayGeometry.Segments,
    stroke: Float,
    strong: Boolean
) {
    val n = geo.count
    val a = geo.a
    val b = geo.b

    val lightAlpha = if (strong) 0.80f else 0.42f
    val darkAlpha = if (strong) 0.65f else 0.30f

    for (i in 0 until n) {
        drawLine(
            color = Color.White.copy(alpha = lightAlpha),
            start = Offset(a[i * 2], a[i * 2 + 1]),
            end = Offset(b[i * 2], b[i * 2 + 1]),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
    for (i in 0 until n) {
        drawLine(
            color = Color.Black.copy(alpha = darkAlpha),
            start = Offset(a[i * 2] + 0.9f, a[i * 2 + 1] + 0.9f),
            end = Offset(b[i * 2] + 0.9f, b[i * 2 + 1] + 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun rememberCrackShaderWithFallback(
    @RawRes fullResId: Int = R.raw.crack_shader,
    @RawRes lightResId: Int = R.raw.crack_shader_light
): RuntimeShader? {
    val context = LocalContext.current

    val fullSource = remember(fullResId) {
        context.resources.openRawResource(fullResId)
            .bufferedReader().use { it.readText() }
    }
    val lightSource = remember(lightResId) {
        context.resources.openRawResource(lightResId)
            .bufferedReader().use { it.readText() }
    }

    return remember(fullSource, lightSource) {
        try {
            RuntimeShader(fullSource)
        } catch (e: IllegalArgumentException) {
            Log.w("CrackShader", "Full shader failed, falling back to light", e)
            try {
                RuntimeShader(lightSource)
            } catch (e2: IllegalArgumentException) {
                Log.e("CrackShader", "Even light shader failed", e2)
                null
            }
        }
    }
}
