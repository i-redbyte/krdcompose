@file:Suppress("unused")

package ru.redbyte.krdcompose.others.mandelbrot

import android.content.Context
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ru.redbyte.krdcompose.R
import kotlin.math.max

/**
 * Лёгкий анимируемый фон множества Мандельброта для Compose на Android 13+ (AGSL RuntimeShader).
 * Работает в отдельном слое View и почти не влияет на расположенные сверху Composable-элементы.
 *
 * Основные фичи:
 * - Настройка центра, зума, лимита итераций, скорости анимации, цветовой палитры (HSV‑градиент).
 * - Рендер на GPU через AGSL (RuntimeShader) – минимум нагрузки на CPU и GC.
 *
 * Требования: API 33+ (Android 13). Для старых версий см. комментарии в конце файла.
 */

@Composable
fun MandelbrotBackground(
    modifier: Modifier = Modifier,
    maxIterations: Int = 300,
    centerX: Float = -0.5f,
    centerY: Float = 0.0f,
    scale: Float = 2.8f,
    animationSpeed: Float = 0.25f,
    hueShift: Float = 0f
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                MandelbrotShaderView(ctx).apply {
                    this.maxIterations = maxIterations
                    this.centerX = centerX
                    this.centerY = centerY
                    this.scale = scale
                    this.animationSpeed = animationSpeed
                    this.hueShift = hueShift
                }
            },
            update = { view ->
                view.maxIterations = maxIterations
                view.centerX = centerX
                view.centerY = centerY
                view.scale = scale
                view.animationSpeed = animationSpeed
                view.hueShift = hueShift
                view.invalidate()
            }
        )
    } else {
        Box(modifier = modifier)
    }
}

@Composable
fun MandelbrotBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        MandelbrotBackground(Modifier.fillMaxSize())
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MandelbrotShaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint()

    private val shader: RuntimeShader by lazy {
        val src = resources.openRawResource(R.raw.mandelbrot)
            .bufferedReader()
            .use { it.readText() }
        RuntimeShader(src)
    }

    var maxIterations: Int = 300
        set(value) {
            field = max(1, value)
        }
    var centerX: Float = -0.5f
    var centerY: Float = 0f
    var scale: Float = 2.8f
    var animationSpeed: Float = 0.25f
    var hueShift: Float = 0f

    private var startTimeMs: Long = System.nanoTime() / 1_000_000L

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shader.setFloatUniform("iResolution", w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val nowMs = System.nanoTime() / 1_000_000L
        val tSec = ((nowMs - startTimeMs) / 1000f) * animationSpeed

        shader.setFloatUniform("uCenter", centerX, centerY)
        shader.setFloatUniform("uScale", scale)
        shader.setFloatUniform("uTime", tSec)
        shader.setFloatUniform("uHueShift", hueShift)
        shader.setFloatUniform("uMaxIter", maxIterations.toFloat())
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        postInvalidateOnAnimation()
    }
}


