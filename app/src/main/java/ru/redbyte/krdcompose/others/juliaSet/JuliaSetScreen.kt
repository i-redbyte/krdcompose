package ru.redbyte.krdcompose.others.juliaSet

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.redbyte.krdcompose.R
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuliaViewerScreen(
    settings: JuliaSettings,
    onUpdate: ((JuliaSettings) -> JuliaSettings) -> Unit,
    onOpenSettings: () -> Unit
) {
    val barState = rememberTopAppBarState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Julia Set") },
                actions = {
                    TextButton(onClick = onOpenSettings) { Text("Настройки") }
                },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    barState
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                FractalSurface(settings = settings)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuliaSettingsScreen(
    settings: JuliaSettings,
    onChange: (JuliaSettings) -> Unit,
    onClose: () -> Unit
) {
    val barState = rememberTopAppBarState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Julia — настройки") },
                navigationIcon = {
                    TextButton(onClick = onClose) { Text("Назад") }
                },
                scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior(
                    barState
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Controls(settings = settings, onChange = onChange)
        }
    }
}

@Composable
fun FractalSurface(settings: JuliaSettings) {
    val tAnim = rememberInfiniteTransition(label = "t")
    val t by tAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "time"
    )
    val time = t * 6.2831853f
    val cRe = if (settings.animateC) (0.7885f * cos(time)) else settings.cRe
    val cIm = if (settings.animateC) (0.7885f * sin(time)) else settings.cIm
    val zoom = if (settings.animateZoom) (1.0f + 0.75f * (sin(time * 0.5f) + 1f)) else settings.zoom
    val centerX = if (settings.animateCenter) (0.25f * sin(time * 0.4f)) else settings.centerX
    val centerY = if (settings.animateCenter) (0.25f * cos(time * 0.33f)) else settings.centerY
    if (settings.useAgsl && Build.VERSION.SDK_INT >= 33) {
        val ctx = LocalContext.current
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                JuliaShaderView(
                    it,
                    ctx.resources.openRawResource(R.raw.julia).bufferedReader()
                        .use { br -> br.readText() })
                    .apply { setHighRes(settings.highRes) }
            },
            update = { view ->
                view.setUniforms(
                    cRe = cRe,
                    cIm = cIm,
                    zoom = zoom,
                    centerX = centerX,
                    centerY = centerY,
                    iterations = settings.iterations,
                    escape = settings.escape,
                    paletteShift = settings.paletteShift,
                    paletteScale = settings.paletteScale,
                    time = time
                )
                view.setHighRes(settings.highRes)
            }
        )
    } else {
        CpuJulia(
            cRe = cRe,
            cIm = cIm,
            zoom = zoom,
            centerX = centerX,
            centerY = centerY,
            iterations = settings.iterations,
            escape = settings.escape,
            paletteShift = settings.paletteShift,
            paletteScale = settings.paletteScale,
            highRes = settings.highRes
        )
    }
}

@Composable
fun CpuJulia(
    cRe: Float,
    cIm: Float,
    zoom: Float,
    centerX: Float,
    centerY: Float,
    iterations: Int,
    escape: Float,
    paletteShift: Float,
    paletteScale: Float,
    highRes: Boolean
) {
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(
        cRe,
        cIm,
        zoom,
        centerX,
        centerY,
        iterations,
        escape,
        paletteShift,
        paletteScale,
        highRes
    ) {
        val w = if (highRes) 1024 else 512
        val h = w
        val scale = 1f / max(w, h).toFloat()
        val data = IntArray(w * h)
        val esc2 = escape * escape
        var y = 0
        while (y < h) {
            val yy = (y - h * 0.5f) * scale
            var x = 0
            while (x < w) {
                val xx = (x - w * 0.5f) * scale
                var zx = (xx / zoom) + centerX
                var zy = (yy / zoom) + centerY
                var i = 0
                var m2 = 0f
                while (i < iterations) {
                    val tx = zx * zx - zy * zy + cRe
                    val ty = 2f * zx * zy + cIm
                    zx = tx
                    zy = ty
                    m2 = zx * zx + zy * zy
                    if (m2 > esc2) break
                    i++
                }
                val t = i.toFloat() / iterations.toFloat()
                val smooth =
                    if (i < iterations && m2 > 0f) t + 1f - ln(ln(sqrt(m2))) / ln(2f) else t
                val hue = ((paletteShift + paletteScale * smooth) % 1f + 1f) % 1f
                val rgb = hsvToRgb(hue, 1f, if (i >= iterations) 0f else 1f)
                data[y * w + x] = Color.rgb(
                    (rgb.first * 255).toInt(),
                    (rgb.second * 255).toInt(),
                    (rgb.third * 255).toInt()
                )
                x++
            }
            y++
        }
        val newBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        newBmp.setPixels(data, 0, w, 0, 0, w, h)
        bmp = newBmp
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val b = bmp
        if (b != null) {
            Image(
                bitmap = b.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(Modifier.fillMaxSize()) {}
        }
    }
}

fun hsvToRgb(h: Float, s: Float, v: Float): Triple<Float, Float, Float> {
    val i = (h * 6f).toInt()
    val f = h * 6f - i
    val p = v * (1f - s)
    val q = v * (1f - f * s)
    val t = v * (1f - (1f - f) * s)
    return when (i % 6) {
        0 -> Triple(v, t, p)
        1 -> Triple(q, v, p)
        2 -> Triple(p, v, t)
        3 -> Triple(p, q, v)
        4 -> Triple(t, p, v)
        else -> Triple(v, p, q)
    }
}

@Composable
fun Controls(settings: JuliaSettings, onChange: (JuliaSettings) -> Unit) {
    val scroll = rememberScrollState()
    var cRe by remember { mutableStateOf(TextFieldValue(settings.cRe.toString())) }
    var cIm by remember { mutableStateOf(TextFieldValue(settings.cIm.toString())) }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AGSL")
            Switch(
                checked = settings.useAgsl && Build.VERSION.SDK_INT >= 33,
                onCheckedChange = { onChange(settings.copy(useAgsl = it && Build.VERSION.SDK_INT >= 33)) })
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Высокое разрешение")
            Switch(
                checked = settings.highRes,
                onCheckedChange = { onChange(settings.copy(highRes = it)) })
        }
        Text("Параметр c (Re, Im)")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = cRe, onValueChange = {
                cRe = it
                it.text.toFloatOrNull()?.let { v -> onChange(settings.copy(cRe = v)) }
            }, modifier = Modifier.weight(1f), label = { Text("Re") })
            OutlinedTextField(value = cIm, onValueChange = {
                cIm = it
                it.text.toFloatOrNull()?.let { v -> onChange(settings.copy(cIm = v)) }
            }, modifier = Modifier.weight(1f), label = { Text("Im") })
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = settings.animateC,
                onCheckedChange = { onChange(settings.copy(animateC = it)) })
            Text("Анимация c")
        }
        Text("Масштаб")
        Slider(
            value = settings.zoom,
            onValueChange = { onChange(settings.copy(zoom = it)) },
            valueRange = 0.5f..5f
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = settings.animateZoom,
                onCheckedChange = { onChange(settings.copy(animateZoom = it)) })
            Text("Анимация масштаба")
        }
        Text("Центр X")
        Slider(
            value = settings.centerX,
            onValueChange = { onChange(settings.copy(centerX = it)) },
            valueRange = -1.5f..1.5f
        )
        Text("Центр Y")
        Slider(
            value = settings.centerY,
            onValueChange = { onChange(settings.copy(centerY = it)) },
            valueRange = -1.5f..1.5f
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = settings.animateCenter,
                onCheckedChange = { onChange(settings.copy(animateCenter = it)) })
            Text("Анимация центра")
        }
        Text("Итерации: ${settings.iterations}")
        Slider(
            value = settings.iterations.toFloat(),
            onValueChange = { onChange(settings.copy(iterations = it.toInt())) },
            valueRange = 50f..1500f
        )
        Text("Порог выхода: ${"%.2f".format(settings.escape)}")
        Slider(
            value = settings.escape,
            onValueChange = { onChange(settings.copy(escape = it)) },
            valueRange = 2f..16f
        )
        Text("Сдвиг палитры: ${"%.2f".format(settings.paletteShift)}")
        Slider(
            value = settings.paletteShift,
            onValueChange = { onChange(settings.copy(paletteShift = it)) },
            valueRange = 0f..1f
        )
        Text("Масштаб палитры: ${"%.2f".format(settings.paletteScale)}")
        Slider(
            value = settings.paletteScale,
            onValueChange = { onChange(settings.copy(paletteScale = it)) },
            valueRange = 0.2f..5f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = {
                onChange(
                    settings.copy(
                        cRe = -0.8f,
                        cIm = 0.156f
                    )
                )
            }) { Text("c=-0.8+0.156i") }
            TextButton(onClick = {
                onChange(
                    settings.copy(
                        cRe = 0.285f,
                        cIm = 0f
                    )
                )
            }) { Text("c=0.285") }
            TextButton(onClick = {
                onChange(
                    settings.copy(
                        cRe = -0.4f,
                        cIm = 0.6f
                    )
                )
            }) { Text("c=-0.4+0.6i") }
        }
    }
}

class JuliaShaderView(context: android.content.Context, source: String) : View(context) {
    private val paint = Paint()
    private val shader: RuntimeShader? =
        if (Build.VERSION.SDK_INT >= 33) RuntimeShader(source) else null
    private var iterations = 300
    private var escape = 4f
    private var cRe = -0.8f
    private var cIm = 0.156f
    private var zoom = 1f
    private var centerX = 0f
    private var centerY = 0f
    private var paletteShift = 0f
    private var paletteScale = 1f
    private var time = 0f
    private var highRes = false

    fun setHighRes(v: Boolean) {
        highRes = v
        invalidate()
    }

    fun setUniforms(
        cRe: Float,
        cIm: Float,
        zoom: Float,
        centerX: Float,
        centerY: Float,
        iterations: Int,
        escape: Float,
        paletteShift: Float,
        paletteScale: Float,
        time: Float
    ) {
        this.cRe = cRe
        this.cIm = cIm
        this.zoom = zoom
        this.centerX = centerX
        this.centerY = centerY
        this.iterations = iterations
        this.escape = escape
        this.paletteShift = paletteShift
        this.paletteScale = paletteScale
        this.time = time
        invalidate()
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        if (w == 0 || h == 0) return
        val s = shader ?: return
        val scale = if (highRes) 2f else 1f
        s.setFloatUniform("resolution", w * scale, h * scale)
        s.setFloatUniform("c", cRe, cIm)
        s.setFloatUniform("zoom", zoom)
        s.setFloatUniform("center", centerX, centerY)
        s.setFloatUniform("escape", escape)
        s.setFloatUniform("time", time)
        s.setFloatUniform("paletteShift", paletteShift)
        s.setFloatUniform("paletteScale", paletteScale)
        s.setIntUniform("iterations", iterations)
        paint.shader = s
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        invalidate()
    }
}

