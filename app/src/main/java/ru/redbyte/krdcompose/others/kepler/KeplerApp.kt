package ru.redbyte.krdcompose.others.kepler

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.redbyte.krdcompose.utils.TrailPathCache
import kotlin.math.*

enum class OrbitType { Circular, Elliptical, Parabolic, Hyperbolic }
enum class BoundaryMode { Bounce, Stop }

data class ColorsConfig(
    val background: Color,
    val star: Color,
    val planet: Color,
    val trail: Color,
    val velocity: Color,
    val acceleration: Color
)

data class VisualConfig(
    val showTrail: Boolean,
    val showVelocity: Boolean,
    val showAcceleration: Boolean
)

data class WorldConstraints(
    val scaleToFit: Boolean,
    val boundaryMode: BoundaryMode,
    val paddingFraction: Float,
    val maxWorldRadiusMultiplier: Float
)

data class OrbitParams(
    val type: OrbitType,
    val initialDistance: Double,
    val eEllipse: Double,
    val eHyper: Double
)

@Composable
fun KeplerApp() {
    var started by rememberSaveable { mutableStateOf(false) }

    val orbitSaver = listSaver<OrbitParams, Any>(
        save = { listOf(it.type.name, it.initialDistance, it.eEllipse, it.eHyper) },
        restore = { list ->
            OrbitParams(
                type = OrbitType.valueOf(list[0] as String),
                initialDistance = (list[1] as Number).toDouble(),
                eEllipse = (list[2] as Number).toDouble(),
                eHyper = (list[3] as Number).toDouble()
            )
        }
    )
    var orbitParams by rememberSaveable(stateSaver = orbitSaver) {
        mutableStateOf(OrbitParams(OrbitType.Circular, 100.0, 0.5, 1.5))
    }

    val colorsSaver = listSaver<ColorsConfig, Any>(
        save = {
            listOf(
                it.background.value.toULong().toLong(),
                it.star.value.toULong().toLong(),
                it.planet.value.toULong().toLong(),
                it.trail.value.toULong().toLong(),
                it.velocity.value.toULong().toLong(),
                it.acceleration.value.toULong().toLong()
            )
        },
        restore = { list ->
            ColorsConfig(
                Color(list[0] as Long),
                Color(list[1] as Long),
                Color(list[2] as Long),
                Color(list[3] as Long),
                Color(list[4] as Long),
                Color(list[5] as Long)
            )
        }
    )
    var colors by rememberSaveable(stateSaver = colorsSaver) {
        mutableStateOf(
            ColorsConfig(
                background = Color(0xFF0B0E16),
                star = Color(0xFFFFD54F),
                planet = Color(0xFF4FC3F7),
                trail = Color(0x80FFFFFF),
                velocity = Color(0xFF66BB6A),
                acceleration = Color(0xFFEF5350)
            )
        )
    }

    val visualsSaver = listSaver<VisualConfig, Any>(
        save = { listOf(it.showTrail, it.showVelocity, it.showAcceleration) },
        restore = { list ->
            VisualConfig(
                list[0] as Boolean,
                list[1] as Boolean,
                list[2] as Boolean
            )
        }
    )
    var visuals by rememberSaveable(stateSaver = visualsSaver) {
        mutableStateOf(
            VisualConfig(
                showTrail = true,
                showVelocity = false,
                showAcceleration = false
            )
        )
    }

    val worldSaver = listSaver<WorldConstraints, Any>(
        save = {
            listOf(
                it.scaleToFit,
                it.boundaryMode.name,
                it.paddingFraction,
                it.maxWorldRadiusMultiplier
            )
        },
        restore = { list ->
            WorldConstraints(
                scaleToFit = list[0] as Boolean,
                boundaryMode = BoundaryMode.valueOf(list[1] as String),
                paddingFraction = (list[2] as Number).toFloat(),
                maxWorldRadiusMultiplier = (list[3] as Number).toFloat()
            )
        }
    )
    var world by rememberSaveable(stateSaver = worldSaver) {
        mutableStateOf(
            WorldConstraints(
                scaleToFit = true,
                boundaryMode = BoundaryMode.Bounce,
                paddingFraction = 0.08f,
                maxWorldRadiusMultiplier = 4f
            )
        )
    }

    if (!started) {
        SettingsScreen(
            orbitParams = orbitParams,
            onOrbitChange = { orbitParams = it },
            colors = colors,
            onColorsChange = { colors = it },
            visuals = visuals,
            onVisualsChange = { visuals = it },
            world = world,
            onWorldChange = { world = it },
            onStart = { started = true }
        )
    } else {
        SimulationScreen(
            orbitParams = orbitParams,
            colors = colors,
            visuals = visuals,
            world = world,
            onBack = { started = false },
            onPreset = { p -> orbitParams = p }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    orbitParams: OrbitParams,
    onOrbitChange: (OrbitParams) -> Unit,
    colors: ColorsConfig,
    onColorsChange: (ColorsConfig) -> Unit,
    visuals: VisualConfig,
    onVisualsChange: (VisualConfig) -> Unit,
    world: WorldConstraints,
    onWorldChange: (WorldConstraints) -> Unit,
    onStart: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kepler Settings") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Тип орбиты", fontWeight = FontWeight.SemiBold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                OrbitType.entries.forEach { t ->
                    FilterChip(
                        selected = orbitParams.type == t,
                        onClick = { onOrbitChange(orbitParams.copy(type = t)) },
                        label = { Text(t.name) }
                    )
                }
            }

            LabeledNumberField(
                label = "Начальная дистанция",
                value = orbitParams.initialDistance,
                onChange = { onOrbitChange(orbitParams.copy(initialDistance = it.coerceAtLeast(1.0))) }
            )

            if (orbitParams.type == OrbitType.Elliptical) {
                LabeledSlider(
                    label = "Эксцентриситет (0..0.99)",
                    value = orbitParams.eEllipse.toFloat(),
                    onChange = { onOrbitChange(orbitParams.copy(eEllipse = it.toDouble())) },
                    valueRange = 0f..0.99f,
                    steps = 0
                )
            }
            if (orbitParams.type == OrbitType.Hyperbolic) {
                LabeledSlider(
                    label = "Эксцентриситет (1.1..5.0)",
                    value = orbitParams.eHyper.toFloat(),
                    onChange = { onOrbitChange(orbitParams.copy(eHyper = it.toDouble())) },
                    valueRange = 1.1f..5f,
                    steps = 0
                )
            }

            Text("Пресеты", fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Circular, 100.0, 0.0, 1.5))
                }, label = { Text("LEO") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Elliptical, 120.0, 0.8, 1.5))
                }, label = { Text("Elliptic") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Parabolic, 100.0, 0.0, 1.5))
                }, label = { Text("Parabolic") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Hyperbolic, 100.0, 0.0, 2.0))
                }, label = { Text("Hyperbolic") })
            }

            Text("Ограничения мира", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = world.scaleToFit,
                    onCheckedChange = { onWorldChange(world.copy(scaleToFit = it)) }
                )
                Text(
                    "Масштабировать, чтобы планета всегда была в кадре",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Поведение на границе:",
                    modifier = Modifier.weight(1f)
                )
                DropdownEnum(
                    current = world.boundaryMode,
                    onSelect = { onWorldChange(world.copy(boundaryMode = it)) }
                )
            }

            LabeledSlider(
                label = "Внутренние поля экрана (доля)",
                value = world.paddingFraction,
                onChange = { onWorldChange(world.copy(paddingFraction = it)) },
                valueRange = 0f..0.2f,
                steps = 0
            )

            LabeledSlider(
                label = "Макс. радиус мира = множитель × нач. дистанции",
                value = world.maxWorldRadiusMultiplier,
                onChange = { onWorldChange(world.copy(maxWorldRadiusMultiplier = it)) },
                valueRange = 2f..10f,
                steps = 8
            )

            Text("Цвета", fontWeight = FontWeight.SemiBold)
            ColorRow("Фон", colors.background) { onColorsChange(colors.copy(background = it)) }
            ColorRow("Звезда", colors.star) { onColorsChange(colors.copy(star = it)) }
            ColorRow("Планета", colors.planet) { onColorsChange(colors.copy(planet = it)) }
            ColorRow("Траектория", colors.trail) { onColorsChange(colors.copy(trail = it)) }
            ColorRow(
                "Вектор скорости",
                colors.velocity
            ) { onColorsChange(colors.copy(velocity = it)) }
            ColorRow("Вектор ускорения", colors.acceleration) {
                onColorsChange(
                    colors.copy(
                        acceleration = it
                    )
                )
            }

            Text("Визуализация", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showTrail,
                    onCheckedChange = { onVisualsChange(visuals.copy(showTrail = it)) }
                )
                Text("Показывать траекторию", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showVelocity,
                    onCheckedChange = { onVisualsChange(visuals.copy(showVelocity = it)) }
                )
                Text("Показывать вектор скорости", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showAcceleration,
                    onCheckedChange = { onVisualsChange(visuals.copy(showAcceleration = it)) }
                )
                Text("Показывать вектор ускорения", modifier = Modifier.padding(start = 8.dp))
            }

            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Старт") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    orbitParams: OrbitParams,
    colors: ColorsConfig,
    visuals: VisualConfig,
    world: WorldConstraints,
    onBack: () -> Unit,
    onPreset: (OrbitParams) -> Unit
) {
    BackHandler(onBack = onBack)
    var warning by rememberSaveable { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1f) }
    var hud by remember { mutableStateOf("") }
    val context = LocalContext.current
    val rootView = LocalView.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Kepler Simulation", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            actions = {
                TextButton(onClick = {
                    running = !running
                }) { Text(if (running) "Пауза" else "Пуск") }
                TextButton(onClick = {
                    val bmp: Bitmap = rootView.drawToBitmap()
                    val uri = saveBitmap(context, bmp, "kepler_${System.currentTimeMillis()}.png")
                    if (uri != null) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(share, "Поделиться снимком"))
                    }
                }) { Text("Снимок") }
            },
            navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } }
        )
    }) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colors.background)
        ) {
            var pos by remember { mutableStateOf(Offset.Zero) }
            var vel by remember { mutableStateOf(Offset.Zero) }
            var acc by remember { mutableStateOf(Offset.Zero) }

            val trailCache = remember {
                TrailPathCache(
                    maxPoints = 800,
                    minSegmentWorld = 0.75f
                )
            }

            var scale by remember { mutableFloatStateOf(1f) }
            var userScale by remember { mutableFloatStateOf(1f) }
            var cameraWorld by remember { mutableStateOf(Offset.Zero) }
            var bounds by remember { mutableStateOf(RectWorld(0f, 0f, 0f, 0f)) }
            var frameScaleToFit by remember { mutableStateOf(world.scaleToFit) }
            var localWarning by remember { mutableStateOf<String?>(null) }

            var futurePath by remember { mutableStateOf<Path?>(null) }

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(world.scaleToFit) {
                        if (!world.scaleToFit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                userScale = (userScale * zoom).coerceIn(0.3f, 5f)
                                val worldDx = pan.x / (scale * userScale)
                                val worldDy = -pan.y / (scale * userScale)
                                cameraWorld += Offset(worldDx, worldDy)
                            }
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx0 = size.width / 2f
                    val cy0 = size.height / 2f
                    val paddingPix = min(size.width, size.height) * world.paddingFraction
                    val halfW = size.width / 2f - paddingPix
                    val halfH = size.height / 2f - paddingPix

                    val worldMaxRadius =
                        orbitParams.initialDistance.toFloat() * world.maxWorldRadiusMultiplier
                    val rNow = pos.getDistance().coerceAtLeast(1e-6f)
                    val scaleFit = min(halfW, halfH) / rNow
                    val minScale = min(halfW, halfH) / worldMaxRadius
                    val baseScale = if (frameScaleToFit) max(scaleFit, minScale) else minScale
                    if (baseScale != scale) scale = baseScale
                    val cx = cx0 - cameraWorld.x * scale * if (frameScaleToFit) 0f else userScale
                    val cy = cy0 + cameraWorld.y * scale * if (frameScaleToFit) 0f else userScale
                    val drawScale = scale * if (frameScaleToFit) 1f else userScale

                    val newBounds = RectWorld(
                        -halfW / drawScale - cameraWorld.x,
                        -halfH / drawScale - cameraWorld.y,
                        halfW / drawScale - cameraWorld.x,
                        halfH / drawScale - cameraWorld.y
                    )
                    if (newBounds != bounds) bounds = newBounds

                    localWarning =
                        if (frameScaleToFit && scaleFit < minScale) "Орбита слишком велика: применены границы" else null

                    drawCircle(colors.star, radius = 10f, center = Offset(cx, cy))

                    if (visuals.showTrail) {
                        trailCache.buildIfNeeded(drawScale, cx, cy)
                        trailCache.draw(this, colors.trail)
                    }

                    futurePath?.let {
                        drawPath(
                            it,
                            color = colors.trail.copy(alpha = 0.6f),
                            style = Stroke(
                                width = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }

                    val px = cx + (pos.x - cameraWorld.x) * drawScale
                    val py = cy - (pos.y - cameraWorld.y) * drawScale
                    drawCircle(colors.planet, radius = 8f, center = Offset(px, py))

                    if (visuals.showVelocity) {
                        val arrow = vel * 0.35f
                        drawArrow(Offset(px, py), arrow * drawScale, colors.velocity)
                    }
                    if (visuals.showAcceleration) {
                        val arrow = acc * 6f
                        drawArrow(Offset(px, py), arrow * drawScale, colors.acceleration)
                    }

                    val mu = 1.0
                    val rx = pos.x.toDouble()
                    val ry = pos.y.toDouble()
                    val vxD = vel.x.toDouble()
                    val vyD = vel.y.toDouble()
                    val rLen = sqrt(rx * rx + ry * ry)
                    val vLen = sqrt(vxD * vxD + vyD * vyD)
                    val eps = 0.5 * vLen * vLen - mu / rLen
                    val a = if (eps < 0) -mu / (2 * eps) else Double.POSITIVE_INFINITY
                    val hz = rx * vyD - ry * vxD
                    val ex = (vyD * hz) / mu - (rx / rLen)
                    val ey = (-vxD * hz) / mu - (ry / rLen)
                    val eMag = sqrt(ex * ex + ey * ey)
                    val T = if (eps < 0) 2.0 * Math.PI * sqrt(a * a * a / mu) else Double.NaN
                    hud = buildString {
                        append("r="); append("%.1f".format(rLen))
                        append("  v="); append("%.3f".format(vLen))
                        append("  e="); append(if (eMag.isFinite()) "%.3f".format(eMag) else "—")
                        append("  a="); append(if (a.isFinite()) "%.1f".format(a) else "∞")
                        if (!T.isNaN()) {
                            append("  T="); append("%.1f".format(T))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = (warning ?: localWarning) != null,
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Surface(color = Color(0xAAE65100), shadowElevation = 4.dp) {
                        Text(
                            text = (warning ?: localWarning) ?: "",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White
                        )
                    }
                }

                Text(
                    hud,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color(0x66000000), shape = CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .background(Color(0x66000000), shape = CircleShape)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Text("Скорость: ${"%.1f".format(speed)}×", color = Color.White)
                    Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.1f..5f)
                }
            }

            LaunchedEffect(orbitParams) {
                val GM = 1.0
                val x = orbitParams.initialDistance
                val y = 0.0
                val vx = 0.0
                val vy = when (orbitParams.type) {
                    OrbitType.Circular -> sqrt(GM / x)
                    OrbitType.Elliptical -> {
                        val e = orbitParams.eEllipse.coerceIn(0.0, 0.99)
                        sqrt(GM * (1 - e) / x)
                    }

                    OrbitType.Parabolic -> sqrt(2 * GM / x)
                    OrbitType.Hyperbolic -> {
                        val e = orbitParams.eHyper.coerceAtLeast(1.1)
                        sqrt(GM * (1 + e) / x)
                    }
                }
                pos = Offset(x.toFloat(), y.toFloat())
                vel = Offset(vx.toFloat(), vy.toFloat())
                acc = Offset.Zero
                trailCache.clear()
                cameraWorld = Offset.Zero
                userScale = 1f
            }

            LaunchedEffect(pos, vel, world.scaleToFit, speed) {
                while (isActive) {
                    val mu = 1.0
                    var x = pos.x.toDouble()
                    var y = pos.y.toDouble()
                    var vx = vel.x.toDouble()
                    var vy = vel.y.toDouble()
                    val path = Path()
                    val steps = 400
                    val horizon = 6.0
                    val dt = horizon / steps * 100.0 * speed
                    var first = true
                    repeat(steps) {
                        val r = sqrt(x * x + y * y).coerceAtLeast(1e-9)
                        val ax = -mu * x / (r * r * r)
                        val ay = -mu * y / (r * r * r)
                        vx += ax * dt
                        vy += ay * dt
                        x += vx * dt
                        y += vy * dt
                        val px = x.toFloat()
                        val py = y.toFloat()
                        if (first) {
                            path.moveTo(px, py)
                            first = false
                        } else path.lineTo(px, py)
                    }
                    futurePath = transformWorldPathToScreen(path, bounds, world.scaleToFit)
                    delay(300)
                }
            }

            LaunchedEffect(Unit) {
                val GM = 1.0
                var last = withFrameNanos { it }
                while (isActive) {
                    val now = withFrameNanos { it }
                    val rawDt = (now - last) / 1e9
                    last = now
                    val dt = if (running) rawDt * 100.0 * speed else 0.0

                    var x = pos.x.toDouble()
                    var y = pos.y.toDouble()
                    var vx = vel.x.toDouble()
                    var vy = vel.y.toDouble()

                    val r = sqrt(x * x + y * y).coerceAtLeast(1e-6)
                    val ax = -GM * x / (r * r * r)
                    val ay = -GM * y / (r * r * r)

                    vx += ax * dt
                    vy += ay * dt
                    x += vx * dt
                    y += vy * dt

                    var newPos = Offset(x.toFloat(), y.toFloat())
                    var newVel = Offset(vx.toFloat(), vy.toFloat())
                    val newAcc = Offset(ax.toFloat(), ay.toFloat())

                    frameScaleToFit = world.scaleToFit
                    val bx = bounds
                    val paddingBehavior = 0.0001f
                    var bounced = false

                    if (!frameScaleToFit) {
                        if (newPos.x < bx.left) {
                            if (world.boundaryMode == BoundaryMode.Bounce) {
                                newPos = newPos.copy(x = bx.left + paddingBehavior)
                                newVel = newVel.copy(x = abs(newVel.x))
                                bounced = true
                            } else {
                                newPos = newPos.copy(x = bx.left)
                                newVel = newVel.copy(x = 0f)
                            }
                        }
                        if (newPos.x > bx.right) {
                            if (world.boundaryMode == BoundaryMode.Bounce) {
                                newPos = newPos.copy(x = bx.right - paddingBehavior)
                                newVel = newVel.copy(x = -abs(newVel.x))
                                bounced = true
                            } else {
                                newPos = newPos.copy(x = bx.right)
                                newVel = newVel.copy(x = 0f)
                            }
                        }
                        if (newPos.y < bx.top) {
                            if (world.boundaryMode == BoundaryMode.Bounce) {
                                newPos = newPos.copy(y = bx.top + paddingBehavior)
                                newVel = newVel.copy(y = abs(newVel.y))
                                bounced = true
                            } else {
                                newPos = newPos.copy(y = bx.top)
                                newVel = newVel.copy(y = 0f)
                            }
                        }
                        if (newPos.y > bx.bottom) {
                            if (world.boundaryMode == BoundaryMode.Bounce) {
                                newPos = newPos.copy(y = bx.bottom - paddingBehavior)
                                newVel = newVel.copy(y = -abs(newVel.y))
                                bounced = true
                            } else {
                                newPos = newPos.copy(y = bx.bottom)
                                newVel = newVel.copy(y = 0f)
                            }
                        }
                        warning = if (bounced) "Столкновение с границей" else null
                    } else {
                        warning = null
                    }

                    pos = newPos
                    vel = newVel
                    acc = newAcc

                    if (visuals.showTrail && running) {
                        trailCache.tryAdd(newPos)
                    }
                }
            }
        }
    }
}

private fun transformWorldPathToScreen(path: Path, bounds: RectWorld, scaleToFit: Boolean): Path {
    return if (scaleToFit) {
        path
    } else {
        path
    }
}

private fun saveBitmap(context: Context, bitmap: Bitmap, name: String): Uri? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kepler")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri =
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
    resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    contentValues.clear()
    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
    resolver.update(uri, contentValues, null, null)
    return uri
}

data class RectWorld(val left: Float, val top: Float, val right: Float, val bottom: Float)

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

fun DrawScope.drawArrow(origin: Offset, vec: Offset, color: Color) {
    val end = origin + vec
    drawLine(color, origin, end, strokeWidth = 2.5f)
    val angle = atan2(vec.y.toDouble(), vec.x.toDouble()).toFloat()
    val headLen = 12f
    val headAngle = 25f * (Math.PI.toFloat() / 180f)
    val left = Offset(
        end.x - headLen * cos(angle - headAngle),
        end.y - headLen * sin(angle - headAngle)
    )
    val right = Offset(
        end.x - headLen * cos(angle + headAngle),
        end.y - headLen * sin(angle + headAngle)
    )
    drawLine(color, end, left, strokeWidth = 2.5f)
    drawLine(color, end, right, strokeWidth = 2.5f)
}
