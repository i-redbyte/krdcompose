package ru.redbyte.krdcompose.games.race

import androidx.compose.runtime.Composable
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.os.Handler
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import kotlin.math.abs
import kotlin.random.Random

enum class ObstacleType { CAR, HOLE, BLOCK }

@Composable
fun RacingGame(onGameOver: (time: Int, score: Int) -> Unit) {
    val context = LocalContext.current
    val sensorManager =
        remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    var carLaneIndex by remember { mutableIntStateOf(0) }
    var timeSec by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var gameRunning by remember { mutableStateOf(false) }
    var frameTick by remember { mutableIntStateOf(0) }
    val obstacles = remember { mutableStateListOf<Obstacle>() }
    var gameAreaSize by remember { mutableStateOf(IntSize.Zero) }
    val laneCount = 3
    var laneWidthPx by remember { mutableFloatStateOf(0f) }
    var carWidth by remember { mutableFloatStateOf(0f) }
    var carHeight by remember { mutableFloatStateOf(0f) }
    var carX by remember { mutableFloatStateOf(0f) }
    var carY by remember { mutableFloatStateOf(0f) }
    val tiltThreshold = 3f
    var tiltTriggeredDirection by remember { mutableIntStateOf(0) }
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!gameRunning) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    if (tiltTriggeredDirection == 0) {
                        if (x > tiltThreshold) {
                            if (carLaneIndex < laneCount - 1) {
                                carLaneIndex += 1
                            }
                            tiltTriggeredDirection = 1
                        } else if (x < -tiltThreshold) {
                            if (carLaneIndex > 0) {
                                carLaneIndex -= 1
                            }
                            tiltTriggeredDirection = -1
                        }
                    } else {
                        if (tiltTriggeredDirection == 1 && x < tiltThreshold / 2) {
                            tiltTriggeredDirection = 0
                        }
                        if (tiltTriggeredDirection == -1 && x > -tiltThreshold / 2) {
                            tiltTriggeredDirection = 0
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }
    DisposableEffect(Unit) {
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME,
            Handler(Looper.getMainLooper())
        )
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }
    val density = LocalDensity.current
    val swipeThresholdPx = remember { with(density) { 50.dp.toPx() } }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Время: $timeSec")
                Text(text = "Очки: $score")
            }
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    while (true) {
                        forEachGesture {
                            awaitPointerEventScope {
                                val down = awaitFirstDown()
                                val up = waitForUpOrCancellation()
                                if (up != null) {
                                    val dx = up.position.x - down.position.x
                                    if (abs(dx) > swipeThresholdPx && gameRunning) {
                                        if (dx > 0 && carLaneIndex < laneCount - 1) {
                                            carLaneIndex += 1
                                        } else if (dx < 0 && carLaneIndex > 0) {
                                            carLaneIndex -= 1
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                .onGloballyPositioned { coords ->
                    gameAreaSize = coords.size
                }) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (gameAreaSize.width > 0 && gameAreaSize.height > 0) {
                        laneWidthPx = gameAreaSize.width.toFloat() / laneCount
                        carWidth = laneWidthPx * 0.8f
                        carHeight = carWidth * 1.5f
                        carLaneIndex = carLaneIndex.coerceIn(0, laneCount - 1)
                        carX = carLaneIndex * laneWidthPx + (laneWidthPx - carWidth) / 2f
                        carY =
                            gameAreaSize.height.toFloat() - carHeight - (gameAreaSize.height * 0.05f)
                    }
                    for (i in 1 until laneCount) {
                        val x = i * laneWidthPx
                        drawLine(
                            Color.DarkGray,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 4f
                        )
                    }
                    obstacles.forEach { obs ->
                        val obsX = obs.lane * laneWidthPx + (laneWidthPx - obs.width) / 2f
                        if (obs.type == ObstacleType.HOLE) {
                            drawCircle(
                                Color.Black,
                                radius = obs.width / 2f,
                                center = Offset(obsX + obs.width / 2f, obs.y + obs.height / 2f)
                            )
                        } else if (obs.type == ObstacleType.BLOCK) {
                            drawRect(
                                Color.Gray,
                                topLeft = Offset(obsX, obs.y),
                                size = Size(obs.width, obs.height)
                            )
                        } else {
                            drawRect(
                                Color.Red,
                                topLeft = Offset(obsX, obs.y),
                                size = Size(obs.width, obs.height)
                            )
                        }
                    }
                    drawRect(
                        Color.Blue,
                        topLeft = Offset(carX, carY),
                        size = Size(carWidth, carHeight)
                    )
                    drawLine(
                        Color.Transparent,
                        start = Offset.Zero,
                        end = Offset.Zero,
                        strokeWidth = frameTick * 0f + 1f
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        while (gameAreaSize.width == 0 || gameAreaSize.height == 0) {
            withFrameNanos {}
        }
        carLaneIndex = laneCount / 2
        gameRunning = true
        val heightPx = gameAreaSize.height.toFloat()
        var lastSpawnTime = 0L
        var spawnIntervalMs = 1000L
        var timeAccumulator = 0f
        var lastFrameTimeNanos = System.nanoTime()
        while (gameRunning) {
            val currentTimeNanos = withFrameNanos { it }
            val deltaTimeSec = (currentTimeNanos - lastFrameTimeNanos) / 1000000000f
            lastFrameTimeNanos = currentTimeNanos
            if (!gameRunning) break
            timeAccumulator += deltaTimeSec
            if (timeAccumulator >= 1f) {
                timeSec += 1
                timeAccumulator -= 1f
            }
            val currentTimeMs = currentTimeNanos / 1000000L
            if (currentTimeMs - lastSpawnTime > spawnIntervalMs) {
                spawnIntervalMs = (500L..1200L).random()
                lastSpawnTime = currentTimeMs
                val lane = Random.nextInt(0, laneCount)
                val type = ObstacleType.values().random()
                val obsWidth = when (type) {
                    ObstacleType.CAR -> carWidth
                    ObstacleType.HOLE -> carWidth * 0.5f
                    ObstacleType.BLOCK -> carWidth * 0.7f
                }
                val obsHeight = when (type) {
                    ObstacleType.CAR -> carHeight
                    ObstacleType.HOLE -> carWidth * 0.5f
                    ObstacleType.BLOCK -> carWidth * 0.7f
                }
                obstacles.add(
                    Obstacle(
                        lane,
                        type,
                        y = -obsHeight,
                        width = obsWidth,
                        height = obsHeight
                    )
                )
            }
            val iterator = obstacles.listIterator()
            while (iterator.hasNext()) {
                val obs = iterator.next()
                val speed = heightPx / 4f
                obs.y += speed * deltaTimeSec
                if (obs.y > heightPx) {
                    iterator.remove()
                    score += 100
                    continue
                }
                val obsX = obs.lane * laneWidthPx + (laneWidthPx - obs.width) / 2f
                val obsLeft = obsX
                val obsRight = obsX + obs.width
                val obsTop = obs.y
                val obsBottom = obs.y + obs.height
                val carLeft = carLaneIndex * laneWidthPx + (laneWidthPx - carWidth) / 2f
                val carRight = carLeft + carWidth
                val carTop = carY
                val carBottom = carY + carHeight
                if (!(obsRight < carLeft || obsLeft > carRight || obsBottom < carTop || obsTop > carBottom)) {
                    gameRunning = false
                    break
                }
            }
            frameTick += 1
        }
        onGameOver(timeSec, score)
    }
}

data class Obstacle(
    val lane: Int,
    val type: ObstacleType,
    var y: Float,
    val width: Float,
    val height: Float
)