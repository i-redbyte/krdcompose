package ru.redbyte.krdcompose.ui.components.kepler

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            onBack = { started = false }
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
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var warning by rememberSaveable { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(true) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Kepler Simulation", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            actions = {
                TextButton(onClick = {
                    running = !running
                }) { Text(if (running) "Пауза" else "Пуск") }
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
            var bounds by remember { mutableStateOf(RectWorld(0f, 0f, 0f, 0f)) }
            var frameScaleToFit by remember { mutableStateOf(world.scaleToFit) }
            var localWarning by remember { mutableStateOf<String?>(null) }

            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val paddingPix = min(size.width, size.height) * world.paddingFraction
                    val halfW = size.width / 2f - paddingPix
                    val halfH = size.height / 2f - paddingPix

                    val worldMaxRadius =
                        orbitParams.initialDistance.toFloat() * world.maxWorldRadiusMultiplier
                    val rNow = pos.getDistance()
                    val scaleFit = if (rNow == 0f) 1f else min(halfW, halfH) / rNow
                    val minScale = min(halfW, halfH) / worldMaxRadius
                    val desiredScale = if (frameScaleToFit) max(scaleFit, minScale) else minScale
                    if (desiredScale != scale) scale = desiredScale

                    val newBounds =
                        RectWorld(-halfW / scale, -halfH / scale, halfW / scale, halfH / scale)
                    if (newBounds != bounds) bounds = newBounds

                    localWarning =
                        if (frameScaleToFit && scaleFit < minScale) "Орбита слишком велика: применены границы" else null

                    drawCircle(colors.star, radius = 10f, center = Offset(cx, cy))

                    if (visuals.showTrail) {
                        // перестраиваем путь только если нужно
                        trailCache.buildIfNeeded(scale, cx, cy)
                        trailCache.draw(this, colors.trail)
                    }

                    val px = cx + pos.x * scale
                    val py = cy - pos.y * scale
                    drawCircle(colors.planet, radius = 8f, center = Offset(px, py))

                    if (visuals.showVelocity) {
                        val arrow = vel * 0.35f
                        drawArrow(Offset(px, py), arrow * scale, colors.velocity)
                    }
                    if (visuals.showAcceleration) {
                        val arrow = acc * 6f
                        drawArrow(Offset(px, py), arrow * scale, colors.acceleration)
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
            }

            LaunchedEffect(Unit) {
                val GM = 1.0
                val timeScale = 100.0
                var last = withFrameNanos { it }
                while (isActive) {
                    val now = withFrameNanos { it }
                    val rawDt = (now - last) / 1e9
                    last = now
                    val dt = if (running) rawDt * timeScale else 0.0

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

                    // обновляем публичные состояния РОВНО один раз за кадр
                    pos = newPos
                    vel = newVel
                    acc = newAcc

                    // добавляем точку в кэш хвоста (с децимацией)
                    if (visuals.showTrail && running) {
                        trailCache.tryAdd(newPos)
                    }
                }
            }
        }
    }
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

//operator fun Offset.times(k: Float): Offset = Offset(x * k, y * k)
//operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)

fun DrawScope.drawArrow(origin: Offset, vec: Offset, color: Color) {
    val end = origin + vec
    drawLine(color, origin, end, strokeWidth = 2.5f)
    val angle = atan2(vec.y.toDouble(), vec.x.toDouble()).toFloat()
    val headLen = 12f
    val headAngle = 25f * (PI.toFloat() / 180f)
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
