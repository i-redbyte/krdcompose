package ru.redbyte.krdcompose.games.race

import androidx.compose.runtime.Composable
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.material3.Text
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import kotlin.random.Random
import ru.redbyte.krdcompose.R
import kotlin.math.min
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.material3.Switch
import androidx.compose.ui.unit.Dp
import ru.redbyte.krdcompose.games.race.ObstacleType.*
import kotlin.math.max
import kotlin.math.roundToInt

enum class ObstacleType { ENEMY_CAR, MINE, CALTROP }

private data class ObstacleState(
    val id: Long,
    val lane: Int,
    val type: ObstacleType,
    val widthPx: Float,
    val heightPx: Float,
    val widthDp: Dp,
    val heightDp: Dp,
    val y: MutableState<Float>
)

@Composable
fun RacingGame(
    modifier: Modifier = Modifier,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
    }

    val playerPainter = painterResource(R.drawable.car_player)
    val enemyPainter = painterResource(R.drawable.car_enemy)
    val minePainter = painterResource(R.drawable.mine)
    val caltropPainter = painterResource(R.drawable.caltrop)

    var gameSize by remember { mutableStateOf(IntSize.Zero) }
    val laneCount = 3
    var laneWidth by remember { mutableFloatStateOf(0f) }

    var carPos by remember { mutableFloatStateOf((laneCount / 2).toFloat()) }
    var carWidth by remember { mutableFloatStateOf(0f) }
    var carHeight by remember { mutableFloatStateOf(0f) }
    var carX by remember { mutableFloatStateOf(0f) }
    var carY by remember { mutableFloatStateOf(0f) }

    val obstacles = remember { mutableStateListOf<ObstacleState>() }
    val maxObstacles = 12

    var running by remember { mutableStateOf(false) }
    var timeSec by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    var showGameOver by remember { mutableStateOf(false) }
    var crashType by remember { mutableStateOf<ObstacleType?>(null) }
    var crashProgress by remember { mutableFloatStateOf(0f) }

    var session by remember { mutableIntStateOf(0) }

    var tiltEnabled by remember { mutableStateOf(true) }
    val tiltThreshold = 2.0f
    var tiltLatch by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val swipePxPerLane = max(laneWidth, 1f)

    DisposableEffect(tiltEnabled) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!running || !tiltEnabled) return
                if (event.sensor.type == TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    if (tiltLatch == 0) {
                        if (x > tiltThreshold) {
                            carPos = (carPos + 1f).coerceIn(0f, (laneCount - 1).toFloat())
                            tiltLatch = 1
                        } else if (x < -tiltThreshold) {
                            carPos = (carPos - 1f).coerceIn(0f, (laneCount - 1).toFloat())
                            tiltLatch = -1
                        }
                    } else {
                        if (tiltLatch == 1 && x < tiltThreshold / 2f) tiltLatch = 0
                        if (tiltLatch == -1 && x > -tiltThreshold / 2f) tiltLatch = 0
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        if (tiltEnabled) sensorManager.registerListener(
            listener,
            accelerometer,
            SENSOR_DELAY_GAME
        )
        onDispose { if (tiltEnabled) sensorManager.unregisterListener(listener) }
    }

    BackHandler(enabled = showGameOver) {}

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = SpaceBetween
        ) {
            Row {
                Text(
                    text = "Время: $timeSec",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(16.dp))
                Text(text = "Очки: $score", fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Наклон")
                Spacer(Modifier.width(8.dp))
                Switch(checked = tiltEnabled, onCheckedChange = { tiltEnabled = it })
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { gameSize = it.size }
                .pointerInput(session) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            if (!running) return@detectHorizontalDragGestures
                            change.consume()
                            carPos = (carPos + dragAmount / swipePxPerLane)
                                .coerceIn(0f, (laneCount - 1).toFloat())
                        },
                        onDragEnd = {
                            carPos = carPos
                                .roundToInt()
                                .coerceIn(0, laneCount - 1)
                                .toFloat()
                        }
                    )
                }
        ) {
            Canvas(Modifier.matchParentSize()) {
                if (gameSize.width > 0 && gameSize.height > 0) {
                    laneWidth = gameSize.width.toFloat() / laneCount
                    val base = min(laneWidth, gameSize.height / 5f)
                    carWidth = base * 0.8f
                    carHeight = carWidth * 1.3f
                    carPos = carPos.coerceIn(0f, (laneCount - 1).toFloat())
                    carX = carPos * laneWidth + (laneWidth - carWidth) / 2f
                    carY = gameSize.height - carHeight - gameSize.height * 0.05f
                }
                for (i in 1 until laneCount) {
                    val x = i * laneWidth
                    drawRect(
                        color = Color(0x332E2E2E),
                        topLeft = Offset(x - 1.5f, 0f),
                        size = Size(3f, size.height)
                    )
                }
            }

            obstacles.forEach { o ->
                val p = when (o.type) {
                    ENEMY_CAR -> enemyPainter
                    MINE -> minePainter
                    CALTROP -> caltropPainter
                }
                val x = o.lane * laneWidth + (laneWidth - o.widthPx) / 2f
                Image(
                    painter = p,
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = x
                            translationY = o.y.value
                        }
                        .size(o.widthDp, o.heightDp)
                )
            }

            val rot = when (crashType) {
                MINE -> 540f * crashProgress
                ENEMY_CAR -> 25f * crashProgress
                CALTROP -> 30f * sin(crashProgress * PI).toFloat()
                null -> 0f
            }
            val yOff = when (crashType) {
                MINE -> {
                    -sin(crashProgress * PI).toFloat() * (gameSize.height * 0.18f)
                }

                CALTROP -> {
                    -sin(crashProgress * PI).toFloat() * (gameSize.height * 0.12f)
                }

                else -> 0f
            }
            val xKnock = when (crashType) {
                ENEMY_CAR -> {
                    val dir = if (carPos < laneCount - 1) 1f else -1f
                    dir * (gameSize.width * 0.12f) * crashProgress
                }

                else -> 0f
            }
            Image(
                painter = playerPainter,
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = carX + xKnock
                        translationY = carY + yOff
                        rotationZ = rot
                    }
                    .size(
                        width = with(density) { carWidth.toDp() },
                        height = with(density) { carHeight.toDp() }
                    )
            )
        }
    }

    LaunchedEffect(session, gameSize) {
        if (gameSize.width == 0 || gameSize.height == 0) return@LaunchedEffect
        carPos = (laneCount / 2).toFloat()
        obstacles.clear()
        timeSec = 0
        score = 0
        running = true
        crashType = null
        crashProgress = 0f
        showGameOver = false

        var lastNs = withFrameNanos { it }
        var secAcc = 0f
        var lastSpawnMs = 0L
        val baseIntervalMs = 900L

        var corridorLane = (laneCount / 2)
        var plannedCorridorLane: Int? = null
        var transitionWindowMs = 0L
        var corridorSinceChangeMs = 0L
        val corridorMinHoldMs = 2200L
        val transitionSafeMs = 900L

        fun canSpawnInLane(lane: Int, minGapPx: Float): Boolean {
            val lastBottom = obstacles.asSequence()
                .filter { it.lane == lane }
                .map { it.y.value + it.heightPx }
                .maxOrNull()
            return lastBottom == null || lastBottom < -minGapPx
        }

        fun spawnObstacle(now: Long, lane: Int, type: ObstacleType, hScreen: Float) {
            val w = when (type) {
                ENEMY_CAR -> min(laneWidth * 0.78f, hScreen * 0.20f)
                MINE -> laneWidth * 0.52f
                CALTROP -> laneWidth * 0.56f
            }
            val obH = if (type == ENEMY_CAR) w * 1.25f else w
            val wDp = with(density) { w.toDp() }
            val hDp = with(density) { obH.toDp() }
            obstacles += ObstacleState(
                id = now + lane + type.ordinal,
                lane = lane,
                type = type,
                widthPx = w,
                heightPx = obH,
                widthDp = wDp,
                heightDp = hDp,
                y = mutableFloatStateOf(-obH - 12f)
            )
        }

        while (isActive) {
            val now = withFrameNanos { it }
            val dt = ((now - lastNs).coerceAtLeast(0)) / 1_000_000_000f
            lastNs = now

            if (running) {
                secAcc += dt
                if (secAcc >= 1f) {
                    timeSec += 1
                    secAcc -= 1f
                }
            }

            val h = gameSize.height.toFloat()
            val speed = h / 3.5f
            val curMs = now / 1_000_000L

            val difficultyK = (1f + min(timeSec, 120) / 120f)
            val jitter = Random.nextInt(-200, 200)
            val targetInterval = (baseIntervalMs / difficultyK + jitter)
                .coerceIn(400f, 1100f)
                .toLong()

            if (running && curMs - lastSpawnMs > targetInterval && obstacles.size < maxObstacles) {
                lastSpawnMs = curMs
                val minGap = h * 0.22f

                val forbidden = buildSet {
                    add(corridorLane)
                    plannedCorridorLane?.let { add(it) }
                }
                val candidate = (0 until laneCount).filter { it !in forbidden }.shuffled()

                var placed = 0
                val toPlace = when (laneCount) {
                    3 -> (1..2).random()
                    else -> (1 until laneCount).random()
                }
                for (lane in candidate) {
                    if (placed >= toPlace) break
                    if (!canSpawnInLane(lane, minGap)) continue
                    val type = when (Random.nextInt(3)) {
                        0 -> ENEMY_CAR
                        1 -> MINE
                        else -> CALTROP
                    }
                    spawnObstacle(now, lane, type, h)
                    placed++
                }

                if (placed == 0) {
                    val fallback = (0 until laneCount).firstOrNull {
                        it != corridorLane && canSpawnInLane(it, minGap)
                    }
                    if (fallback != null) {
                        spawnObstacle(
                            now,
                            lane = fallback,
                            type = ENEMY_CAR,
                            hScreen = h
                        )
                    }
                }
            }

            val it = obstacles.listIterator()
            while (it.hasNext()) {
                val o = it.next()
                if (running) o.y.value = o.y.value + speed * dt
                if (o.y.value > gameSize.height) {
                    it.remove()
                    if (running) score += 100
                }
            }

            if (running) {
                val carLeft = carPos * laneWidth + (laneWidth - carWidth) / 2f
                val carTop = carY
                val carHit = insetRectByScale(
                    carLeft,
                    carTop,
                    carLeft + carWidth,
                    carTop + carHeight,
                    0.75f
                )
                loop@ for (o in obstacles) {
                    val ox = o.lane * laneWidth + (laneWidth - o.widthPx) / 2f
                    val scale = when (o.type) {
                        ENEMY_CAR -> 0.70f
                        MINE -> 0.55f
                        CALTROP -> 0.60f
                    }
                    val oHit = insetRectByScale(
                        ox,
                        o.y.value,
                        ox + o.widthPx,
                        o.y.value + o.heightPx,
                        scale
                    )
                    if (overlap(carHit, oHit)) {
                        running = false
                        crashType = o.type
                        crashProgress = 0f
                        break@loop
                    }
                }
            } else if (crashType != null) {
                crashProgress = (crashProgress + dt / 0.9f).coerceAtMost(1f)
                if (crashProgress >= 1f) {
                    showGameOver = true
                    break
                }
            }

            corridorSinceChangeMs += (dt * 1000).toLong()
            if (plannedCorridorLane != null) {
                transitionWindowMs -= (dt * 1000).toLong()
                if (transitionWindowMs <= 0L) {
                    corridorLane = plannedCorridorLane
                    plannedCorridorLane = null
                    corridorSinceChangeMs = 0L
                }
            } else if (corridorSinceChangeMs >= corridorMinHoldMs && running) {
                val dir = listOf(-1, 1)
                    .filter { (corridorLane + it) in 0 until laneCount }
                    .randomOrNull()
                if (dir != null) {
                    plannedCorridorLane = (corridorLane + dir).coerceIn(0, laneCount - 1)
                    transitionWindowMs = transitionSafeMs
                }
            }
        }
    }

    if (showGameOver) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(
                    onClick = {
                        showGameOver = false
                        session += 1
                    }
                ) { Text("Повторить") }
            },
            dismissButton = {
                TextButton(
                    onClick = { onExit() }
                ) { Text("Выйти") }
            },
            title = { Text("Игра окончена") },
            text = { Text("Время: $timeSec сек\nОчки: $score") }
        )
    }
}

private fun insetRectByScale(l: Float, t: Float, r: Float, b: Float, scale: Float): Rect {
    val cx = (l + r) * 0.5f
    val cy = (t + b) * 0.5f
    val hw = (r - l) * 0.5f * scale
    val hh = (b - t) * 0.5f * scale
    return Rect(cx - hw, cy - hh, cx + hw, cy + hh)
}

private fun overlap(a: Rect, b: Rect): Boolean {
    return a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top
}
