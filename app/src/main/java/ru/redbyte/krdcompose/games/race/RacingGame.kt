package ru.redbyte.krdcompose.games.race

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import ru.redbyte.krdcompose.R
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

sealed interface RoadItem{
    val ordinal: Int
}
enum class ObstacleType : RoadItem { ENEMY_CAR, MINE, CALTROP }
enum class WeaponType : RoadItem { BERDANKA, MAXIM, EMPTY }

data class WeaponState(
    val type: WeaponType,
    val shotInterval: Long,
    val countBullet: MutableState<Int>,
    val bullets: SnapshotStateList<Bullet>
)

private fun WeaponType.generateWeapon(): WeaponState =
    WeaponState(
        type = this,
        shotInterval = when (this) {
            WeaponType.BERDANKA -> 1_000_000_000L
            WeaponType.MAXIM -> 300_000_000L
            WeaponType.EMPTY -> 0L
        },
        countBullet = mutableIntStateOf(
            when (this) {
                WeaponType.BERDANKA -> 3
                WeaponType.MAXIM -> 10
                WeaponType.EMPTY -> 0
            }
        ),
        bullets = mutableStateListOf()
    )

data class Bullet(
    val x: MutableState<Float> = mutableFloatStateOf(0f),
    val y: MutableState<Float> = mutableFloatStateOf(0f),
    val radius: Float = 15f

) {
    val center: Offset
        get() = Offset(x.value, y.value)
    val rect: Rect
        get() = Rect(
            center = center,
            radius = radius
        )
}


private data class ObstacleState(
    val id: Long,
    val lane: Int,
    val type: RoadItem,
    val widthPx: Float,
    val heightPx: Float,
    val widthDp: Dp,
    val heightDp: Dp,
    val y: MutableState<Float>
)

/**
 * [RacingGame] — Jetpack Compose-компонент, реализующий аркадную игру «Гонки».
 *
 * ## Архитектура и устройство
 *
 * ### Игровой цикл
 * - Игровой цикл реализован внутри `LaunchedEffect` с использованием [withFrameNanos].
 * - Каждый кадр вычисляется прошедшее время `dt` (в секундах), на основе которого:
 *   - обновляется позиция автомобиля игрока (через ввод);
 *   - обновляются координаты препятствий (скорость пропорциональна высоте экрана: `speed = screenHeight / 3.5f`);
 *   - проверяются столкновения;
 *   - инкрементируется секундомер.
 *
 * ### Управление
 * - **Свайпы**: обработка через [detectHorizontalDragGestures].
 *   - Во время drag автомобиль смещается пропорционально `dragAmount / laneWidth`.
 *   - После окончания жеста позиция «привязывается» к ближайшей полосе (`roundToInt()`).
 * - **Акселерометр**: события от [Sensor.TYPE_ACCELEROMETER].
 *   - Используется «порог» (`tiltThreshold`) для исключения дребезга.
 *   - Наклон влево/вправо смещает машину на одну полосу.
 *   - Инверсия сторон учтена: положительный X => движение вправо.
 * - Режим управления переключается через [Switch] в UI.
 *
 * ### Генерация препятствий
 * - Каждое препятствие имеет тип ([ObstacleType]) и параметры размеров.
 * - Интервал спавна = `baseIntervalMs / difficultyK ± jitter`:
 *   - `baseIntervalMs` = 900 мс.
 *   - `difficultyK` линейно растёт от 1.0 до 2.0 за первые 120 секунд игры.
 *   - `jitter` — случайный сдвиг в пределах ±200 мс.
 * - В каждый тик гарантируется «коридор» — хотя бы одна полоса остаётся свободной.
 * - Для исключения блокировки всех полос используются:
 *   - пер-полосные кулдауны `laneCooldownUntilMs`;
 *   - «безопасное окно» при смене коридора (обе полосы временно запрещены для спавна).
 *
 * ### Коллизии
 * - Хитбоксы автомобилей и препятствий аппроксимируются прямоугольниками [Rect].
 * - Для точности хитбоксы уменьшаются (`insetRectByScale`) на коэффициент:
 *   - ENEMY_CAR = 0.70f
 *   - MINE = 0.55f
 *   - CALTROP = 0.60f
 *   - Игрок = 0.75f
 * - Проверка: AABB-коллизия (`a.left < b.right && a.right > b.left ...`).
 * - При пересечении:
 *   - игра останавливается (`running = false`);
 *   - фиксируется [crashType];
 *   - запускается анимация аварии.
 *
 * ### Анимации аварий
 * - Рассчитываются по времени с момента столкновения (`crashProgress ∈ [0..1]`).
 * - Типы:
 *   - MINE: вертикальный подброс `-sin(progress * PI) * h*0.18` + вращение 540°.
 *   - ENEMY_CAR: смещение в сторону +25° вращение.
 *   - CALTROP: подскок `-sin(progress * PI) * h*0.12` + колебания ±30°.
 * - После завершения (`progress >= 1f`) показывается диалог «Игра окончена».
 *
 * ### Отрисовка
 * - Фон:
 *   - Тёмный прямоугольник дороги.
 *   - Боковые линии (`drawRect`).
 *   - Прерывистая разметка (цикл прямоугольников с фазовым сдвигом `roadOffset`).
 * - Игровые объекты:
 *   - Автомобиль игрока и препятствия отрисовываются через [Image] и [graphicsLayer].
 *   - Положение задаётся напрямую: `translationX`, `translationY`.
 *   - Это даёт GPU-ускорение и минимальную нагрузку.
 * - Обрезка: весь Box и Canvas используют `clipToBounds()`, чтобы объекты не выходили за поле.
 *
 * ## API
 *
 * @param modifier [Modifier] для внешней настройки контейнера игры.
 * @param onExit Callback, вызываемый при нажатии кнопки «Выйти» в диалоге окончания игры.
 * @param speed Параметр скорости. При `1f` игра работает в базовом режиме,
 *
 * ## Пример интеграции
 * ```
 * Scaffold { innerPadding ->
 *     RacingGame(
 *         modifier = Modifier
 *             .fillMaxSize()
 *             .padding(innerPadding),
 *         onExit = { navController.popBackStack() },
 *         speed = 1.5f
 *     )
 * }
 * ```
 *
 * ## Требования к ресурсам
 * - В проект должны быть добавлены PNG-изображения:
 *   - `car_player.png`
 *   - `car_enemy.png`
 *   - `mine.png`
 *   - `caltrop.png`
 * - Рекомендуемый размер: 256×256 px, прозрачный фон.
 *
 * ## Ограничения
 * - Горизонтальная ориентация экрана не поддерживается.
 * - Количество полос зашито в 3 (можно расширить при модификации кода).
 * - Сложность фиксирована: растёт только частота спавна, скорости остаются постоянными.
 *
 * @see ObstacleType
 */
@Composable
fun RacingGame(
    modifier: Modifier = Modifier,
    onExit: () -> Unit,
    speed: Float = 1f
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val playerPainter = painterResource(R.drawable.mad_biker) //car_player
    val enemyPainter = painterResource(R.drawable.car_enemy)
    val minePainter = painterResource(R.drawable.mine)
    val caltropPainter = painterResource(R.drawable.caltrop)
    val berdankaPainter = painterResource(R.drawable.berd)
    val maximPainter = painterResource(R.drawable.maxim)

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

    var roadOffset by remember { mutableFloatStateOf(0f) }
    val stripeLenPx = max(24f, laneWidth * 0.35f)
    val stripeGapPx = stripeLenPx
    val weaponState: MutableState<WeaponState> = remember {
        mutableStateOf(WeaponType.EMPTY.generateWeapon())

    }


    DisposableEffect(tiltEnabled) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!running || !tiltEnabled) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    if (tiltLatch == 0) {
                        if (x > tiltThreshold) {
                            carPos =
                                (carPos + 1f).coerceIn(0f, (laneCount - 1).toFloat()); tiltLatch = 1
                        } else if (x < -tiltThreshold) {
                            carPos =
                                (carPos - 1f).coerceIn(0f, (laneCount - 1).toFloat()); tiltLatch =
                                -1
                        }
                    } else {
                        if (tiltLatch == 1 && x < tiltThreshold / 2f) tiltLatch = 0
                        if (tiltLatch == -1 && x > -tiltThreshold / 2f) tiltLatch = 0
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        if (tiltEnabled) sensorManager.registerListener(listener, accelerometer, SENSOR_DELAY_GAME)
        onDispose { if (tiltEnabled) sensorManager.unregisterListener(listener) }
    }

    BackHandler(enabled = showGameOver) {}

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text("Время: $timeSec", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Text("Очки: $score", fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Наклон")
                Spacer(Modifier.width(8.dp))
                Switch(checked = tiltEnabled, onCheckedChange = { tiltEnabled = it })
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .onGloballyPositioned { gameSize = it.size }
                .pointerInput(session) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            if (!running) return@detectHorizontalDragGestures
                            change.consume()
                            carPos = (carPos + dragAmount / swipePxPerLane).coerceIn(
                                0f,
                                (laneCount - 1).toFloat()
                            )
                        },
                        onDragEnd = {
                            carPos = carPos.roundToInt().coerceIn(0, laneCount - 1).toFloat()
                        }
                    )
                }
        ) {
            Canvas(
                Modifier
                    .matchParentSize()
                    .clipToBounds()
            ) {
                if (gameSize.width > 0 && gameSize.height > 0) {
                    laneWidth = gameSize.width.toFloat() / laneCount
                    val base = min(laneWidth, gameSize.height / 5f)
                    carWidth = base * 0.8f
                    carHeight = carWidth * 1.3f
                    carPos = carPos.coerceIn(0f, (laneCount - 1).toFloat())
                    carX = carPos * laneWidth + (laneWidth - carWidth) / 2f
                    carY = gameSize.height - carHeight - gameSize.height * 0.05f
                }

                drawRect(Color(0xFF101010), size = size)
                drawRect(Color(0xFFCCCCCC), topLeft = Offset(0f, 0f), size = Size(6f, size.height))
                drawRect(
                    Color(0xFFCCCCCC),
                    topLeft = Offset(size.width - 6f, 0f),
                    size = Size(6f, size.height)
                )
                weaponState.value.bullets.forEach { bullet ->
                    drawCircle(
                        Color.Red,
                        radius = bullet.radius,
                        center = bullet.center
                    )
                }


                val totalLen = stripeLenPx + stripeGapPx
                for (i in 1 until laneCount) {
                    val x = i * laneWidth
                    var y = -stripeLenPx + (roadOffset % totalLen)
                    while (y < size.height) {
                        drawRect(
                            color = Color(0x66FFFFFF),
                            topLeft = Offset(x - 2f, y),
                            size = Size(4f, stripeLenPx)
                        )
                        y += totalLen
                    }
                }
            }

            obstacles.forEach { o ->
                val p = when (o.type) {
                    ObstacleType.ENEMY_CAR -> enemyPainter
                    ObstacleType.MINE -> minePainter
                    ObstacleType.CALTROP -> caltropPainter
                    WeaponType.BERDANKA -> berdankaPainter
                    WeaponType.MAXIM -> maximPainter
                    WeaponType.EMPTY -> ColorPainter(Color.Transparent)
                }
                val x = o.lane * laneWidth + (laneWidth - o.widthPx) / 2f
                Image(
                    painter = p,
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer { translationX = x; translationY = o.y.value }
                        .size(o.widthDp, o.heightDp)
                )
            }

            val rot = when (crashType) {
                ObstacleType.MINE -> 540f * crashProgress
                ObstacleType.ENEMY_CAR -> 25f * crashProgress
                ObstacleType.CALTROP -> 30f * sin(crashProgress * PI).toFloat()
                null -> 0f
            }
            val yOff = when (crashType) {
                ObstacleType.MINE -> -sin(crashProgress * PI).toFloat() * (gameSize.height * 0.18f)
                ObstacleType.CALTROP -> -sin(crashProgress * PI).toFloat() * (gameSize.height * 0.12f)
                else -> 0f
            }
            val xKnock = when (crashType) {
                ObstacleType.ENEMY_CAR -> {
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
                        translationX = carX + xKnock; translationY = carY + yOff; rotationZ = rot
                    }
                    .size(with(density) { carWidth.toDp() }, with(density) { carHeight.toDp() })
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
        roadOffset = 0f

        var lastNs = withFrameNanos { it }
        var secAcc = 0f
        var lastSpawnMs = 0L
        var lastShout = 0L
        val baseIntervalMs = 900L
        val minIntervalMs = 380L
        val maxIntervalMs = 1100L

        val baseCorridorMinHoldMs = 2200L
        val baseTransitionSafeMs = 900L
        val baseCorridorCooldownMs = 1400L
        val baseTransitionDualCooldownMs = 900L

        var corridorLane = (laneCount / 2)
        var plannedCorridorLane: Int? = null
        var transitionWindowMs = 0L
        var corridorSinceChangeMs = 0L

        val laneCooldownUntilMs = LongArray(laneCount) { 0L }

        fun canSpawnInLane(lane: Int, minGapPx: Float): Boolean {
            val lastBottom =
                obstacles.asSequence().filter { it.lane == lane }.map { it.y.value + it.heightPx }
                    .maxOrNull()
            return lastBottom == null || lastBottom < -minGapPx
        }

        fun spawnObstacle(now: Long, lane: Int, type: RoadItem, hScreen: Float) {
            val w = when (type) {
                ObstacleType.ENEMY_CAR -> min(laneWidth * 0.78f, hScreen * 0.20f)
                ObstacleType.MINE -> laneWidth * 0.52f
                ObstacleType.CALTROP -> laneWidth * 0.56f
                WeaponType.BERDANKA -> laneWidth * 0.56f
                WeaponType.MAXIM -> laneWidth * 0.56f
                WeaponType.EMPTY -> laneWidth * 0.56f
            }
            val obH = if (type == ObstacleType.ENEMY_CAR) w * 1.25f else w
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
            val h = gameSize.height.toFloat()
            val baseSpeed = h / 3.5f
            val speedPx = baseSpeed * speed

            if (running) {
                secAcc += dt
                if (secAcc >= 1f) {
                    timeSec += 1; secAcc -= 1f
                }
                roadOffset = (roadOffset + speedPx * dt) % (stripeLenPx + stripeGapPx)
            }

            val difficultyK = (1f + min(timeSec, 120) / 120f)
            val jitter = Random.nextInt(-200, 200)
            val spawnInterval = (baseIntervalMs / (speed * difficultyK) + jitter).coerceIn(
                (minIntervalMs / speed).toFloat(),
                (maxIntervalMs / speed).toFloat()
            ).toLong()

            if (running && now / 1_000_000L - lastSpawnMs > spawnInterval && obstacles.size < maxObstacles) {
                lastSpawnMs = now / 1_000_000L
                val minGap = (h * 0.22f / speed).coerceIn(h * 0.12f, h * 0.35f)

                val forbid = buildSet { add(corridorLane); plannedCorridorLane?.let { add(it) } }
                val candidate =
                    (0 until laneCount).filter { it !in forbid && now / 1_000_000L >= laneCooldownUntilMs[it] }
                        .shuffled()

                var placed = 0
                val toPlace = when (laneCount) {
                    3 -> (1..2).random(); else -> (1 until laneCount).random()
                }
                for (lane in candidate) {
                    if (placed >= toPlace) break
                    if (!canSpawnInLane(lane, minGap)) continue
                    val type = when (Random.nextInt(6)) {
                        0 -> ObstacleType.ENEMY_CAR;
                        1 -> ObstacleType.MINE;
                        2 -> ObstacleType.CALTROP
                        4 -> WeaponType.BERDANKA
                        5 -> WeaponType.MAXIM
                        6 -> WeaponType.EMPTY
                        else -> WeaponType.EMPTY
                    }
                    spawnObstacle(now, lane, type, h)
                    placed++
                }
                if (placed == 0) {
                    val fallback = (0 until laneCount).firstOrNull {
                        it != corridorLane && now / 1_000_000L >= laneCooldownUntilMs[it] && canSpawnInLane(
                            it,
                            minGap
                        )
                    }
                    if (fallback != null) spawnObstacle(now, fallback, ObstacleType.ENEMY_CAR, h)
                }
            }

            val it = obstacles.listIterator()
            while (it.hasNext()) {
                val o = it.next()
                if (running) o.y.value = o.y.value + speedPx * dt
                if (o.y.value > gameSize.height) {
                    it.remove(); if (running) score += 100
                }
            }

            val bulletsToRemove = mutableListOf<Bullet>()
            if (now - lastShout > weaponState.value.shotInterval / speed) {
                if (weaponState.value.countBullet.value > 0) {
                    weaponState.value.bullets.add(
                        Bullet(
                            x = mutableFloatStateOf(carX + carWidth / 2),
                            y = mutableFloatStateOf(carY - 20f)
                        )
                    )
                    lastShout = now
                    weaponState.value.countBullet.value -= 1
                }

            }

            weaponState.value.bullets.forEach { bullet ->
                val newY = bullet.y.value - speedPx * dt * 1.05f
                bullet.y.value = newY
                if (newY < 0) {
                    bulletsToRemove.add(bullet)
                }
            }
            weaponState.value.bullets.removeAll(bulletsToRemove)


            if (running) {
                val carLeft = carPos * laneWidth + (laneWidth - carWidth) / 2f
                val carTop = carY
                val carHit =
                    insetRectByScale(carLeft, carTop, carLeft + carWidth, carTop + carHeight, 0.75f)
                val obstaclesToRemove = mutableSetOf<ObstacleState>()
                loop@ for (o in obstacles) {
                    val ox = o.lane * laneWidth + (laneWidth - o.widthPx) / 2f
                    val scale = when (o.type) {
                        ObstacleType.ENEMY_CAR -> 0.70f;
                        ObstacleType.MINE -> 0.55f;
                        ObstacleType.CALTROP -> 0.60f
                        WeaponType.BERDANKA -> 0.5f
                        WeaponType.MAXIM -> 0.8f
                        WeaponType.EMPTY -> 0f
                    }
                    val oHit = insetRectByScale(
                        ox,
                        o.y.value,
                        ox + o.widthPx,
                        o.y.value + o.heightPx,
                        scale
                    )
                    weaponState.value.bullets.forEach { bullet ->
                        if (overlap(bullet.rect, oHit)) {
                            obstaclesToRemove.add(o)
                            score += 100
                        }
                    }
                    if (overlap(carHit, oHit)) {
                        when (o.type) {
                            is ObstacleType -> {
                                running = false; crashType = o.type; crashProgress =
                                    0f; break@loop
                            }

                            is WeaponType -> {
                                obstaclesToRemove.add(o)
                                weaponState.value = o.type.generateWeapon()
                            }
                        }

                    }
                }
                obstacles.removeAll(obstaclesToRemove)
                obstacles.removeAll(obstaclesToRemove)
            } else if (crashType != null) {
                crashProgress = (crashProgress + dt / 0.9f).coerceAtMost(1f)
                if (crashProgress >= 1f) {
                    showGameOver = true; break
                }
            }

            corridorSinceChangeMs += (dt * 1000).toLong()
            val corridorMinHoldMs = (baseCorridorMinHoldMs / speed).toLong().coerceAtLeast(600L)
            val transitionSafeMs = (baseTransitionSafeMs / speed).toLong().coerceAtLeast(300L)
            val corridorCooldownMs = (baseCorridorCooldownMs / speed).toLong().coerceAtLeast(400L)
            val transitionDualCooldownMs =
                (baseTransitionDualCooldownMs / speed).toLong().coerceAtLeast(300L)

            if (plannedCorridorLane != null) {
                transitionWindowMs -= (dt * 1000).toLong()
                if (transitionWindowMs <= 0L) {
                    corridorLane = plannedCorridorLane!!; plannedCorridorLane =
                        null; corridorSinceChangeMs = 0L
                    laneCooldownUntilMs[corridorLane] = now / 1_000_000L + corridorCooldownMs
                }
            } else if (corridorSinceChangeMs >= corridorMinHoldMs && running) {
                val dir =
                    listOf(-1, 1).filter { (corridorLane + it) in 0 until laneCount }.randomOrNull()
                if (dir != null) {
                    plannedCorridorLane = (corridorLane + dir).coerceIn(0, laneCount - 1)
                    transitionWindowMs = transitionSafeMs
                    laneCooldownUntilMs[corridorLane] = now / 1_000_000L + transitionDualCooldownMs
                    laneCooldownUntilMs[plannedCorridorLane!!] =
                        now / 1_000_000L + transitionDualCooldownMs
                }
            }
        }
    }


    if (showGameOver) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    showGameOver = false; session += 1
                }) { Text("Повторить") }
            },
            dismissButton = { TextButton(onClick = { onExit() }) { Text("Выйти") } },
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
