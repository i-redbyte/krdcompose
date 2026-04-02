package ru.redbyte.krdcompose.ui.components.lightning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun LightningBackground(
    modifier: Modifier = Modifier,
    config: LightningConfig = LightningConfig(),
    theme: LightningTheme = LightningThemes.BlueStorm,
    content: @Composable BoxScope.() -> Unit
) {
    val random = remember { Random(System.currentTimeMillis()) }
    val bolts = remember { mutableStateListOf<LightningBolt>() }
    val shockRings = remember { mutableStateListOf<ShockRingState>() }

    var plasma by remember { mutableStateOf<PlasmaState?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var timeMs by remember { mutableLongStateOf(0L) }
    val viewConfig = LocalViewConfiguration.current

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frame ->
                timeMs = frame / 1_000_000L
            }
        }
    }

    fun spawnShockRing(center: Offset) {
        if (!config.shockRingEnabled) return
        shockRings += ShockRingState(center = center, bornAtMs = timeMs, lifeMs = 420L)
    }

    fun spawnBolt(
        points: List<Offset>,
        branches: List<List<Offset>>,
        flashScale: Float = 1f,
        minLife: Long = 110L,
        maxLife: Long = 220L,
        minStroke: Float = 1.3f,
        maxStroke: Float = 2.5f,
        minGlow: Float = 6f,
        maxGlow: Float = 13f,
        isPlasmaBolt: Boolean = false
    ) {
        bolts += LightningBolt(
            id = random.nextLong(),
            points = points,
            branches = branches,
            bornAtMs = timeMs,
            lifeMs = random.nextLong(minLife, maxLife),
            strokeWidth = random.nextDouble(minStroke.toDouble(), maxStroke.toDouble()).toFloat(),
            glowWidth = random.nextDouble(minGlow.toDouble(), maxGlow.toDouble()).toFloat(),
            alpha = random.nextDouble(0.78, 1.0).toFloat(),
            flashStrength = random.nextDouble(0.65, 1.0).toFloat() * flashScale,
            isPlasmaBolt = isPlasmaBolt
        )
    }

    fun spawnRandomBolt() {
        if (!config.enabled || canvasSize.width <= 1f || canvasSize.height <= 1f) return

        val start = Offset(
            x = random.nextFloat() * canvasSize.width,
            y = random.nextFloat() * canvasSize.height
        )

        val angle = random.nextFloat() * 360f
        val dir = angleToUnitVector(angle)
        val end = rayToBounds(start, dir, canvasSize)

        val main = generateLightningPath(
            start = start,
            end = end,
            random = random,
            jitter = 26f,
            minSegments = 10,
            maxSegments = 20
        )

        val branches = generateBranches(main, random, 1..4, 0.34f)
        spawnBolt(main, branches, flashScale = 1f)
    }

    fun spawnTouchBolt(touch: Offset) {
        if (!config.enabled || canvasSize.width <= 1f || canvasSize.height <= 1f) return

        val angle = random.nextFloat() * 360f
        val dir = angleToUnitVector(angle)
        val end = rayToBounds(touch, dir, canvasSize)

        val main = generateLightningPath(
            start = touch,
            end = end,
            random = random,
            jitter = 22f,
            minSegments = 8,
            maxSegments = 17
        )

        val branches = generateBranches(main, random, 1..3, 0.24f)
        spawnBolt(main, branches, flashScale = 0.82f)
    }

    fun spawnPlasmaBurst(center: Offset) {
        if (!config.enabled || canvasSize.width <= 1f || canvasSize.height <= 1f) return

        repeat(random.nextInt(4, 8)) {
            val baseAngle = random.nextFloat() * 360f
            val dir = angleToUnitVector(baseAngle)

            val start = if (config.plasmaAnchorRingEnabled) {
                val ringRadius = random.nextDouble(16.0, 30.0).toFloat()
                clampToBounds(center + dir * ringRadius, canvasSize)
            } else {
                center
            }

            val outwardAngle = baseAngle + random.nextDouble(-30.0, 30.0).toFloat()
            val outwardDir = angleToUnitVector(outwardAngle)
            val distance = random.nextDouble(18.0, 58.0).toFloat()
            val target = clampToBounds(start + outwardDir * distance, canvasSize)

            val main = generateLightningPath(
                start = start,
                end = target,
                random = random,
                jitter = 5.5f,
                minSegments = 3,
                maxSegments = 6
            )

            val branches = generateBranches(main, random, 0..1, 0.18f)

            spawnBolt(
                points = main,
                branches = branches,
                flashScale = 0.10f,
                minLife = 60L,
                maxLife = 110L,
                minStroke = 0.9f,
                maxStroke = 1.5f,
                minGlow = 3f,
                maxGlow = 7f,
                isPlasmaBolt = true
            )
        }
    }

    LaunchedEffect(config.enabled, config.autoRandomEnabled, canvasSize) {
        while (config.enabled && config.autoRandomEnabled) {
            if (canvasSize.width > 1f && canvasSize.height > 1f) {
                spawnRandomBolt()
            }
            delay(random.nextLong(config.autoMinDelayMs, config.autoMaxDelayMs + 1))
        }
    }

    LaunchedEffect(
        plasma?.pointerId,
        plasma?.position,
        config.enabled,
        config.longPressPlasmaEnabled
    ) {
        while (config.enabled && config.longPressPlasmaEnabled && plasma != null) {
            spawnPlasmaBurst(plasma!!.position)
            delay(random.nextLong(20L, 36L))
        }
    }

    LaunchedEffect(timeMs) {
        val now = timeMs
        bolts.removeAll { now - it.bornAtMs > it.lifeMs }
        shockRings.removeAll { now - it.bornAtMs > it.lifeMs }
    }

    Box(
        modifier = modifier.background(theme.background)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
        ) {
            val now = timeMs
            var flashAccumulator = 0f

            if (config.plasmaAfterimageEnabled) {
                bolts.forEach { bolt ->
                    if (!bolt.isPlasmaBolt) return@forEach
                    val age = (now - bolt.bornAtMs).coerceAtLeast(0)
                    val t = (age / bolt.lifeMs.toFloat()).coerceIn(0f, 1f)
                    val fade = 1f - t
                    val ghostAlpha = 0.18f * fade

                    drawLightningPath(
                        points = bolt.points,
                        coreColor = theme.lightningGlow.copy(alpha = ghostAlpha),
                        glowColor = theme.lightningGlow.copy(alpha = ghostAlpha * 0.6f),
                        strokeWidth = bolt.strokeWidth * 2.2f,
                        glowWidth = bolt.glowWidth * 1.8f
                    )
                }
            }

            shockRings.forEach { ring ->
                val age = (now - ring.bornAtMs).coerceAtLeast(0)
                val t = (age / ring.lifeMs.toFloat()).coerceIn(0f, 1f)
                val fade = 1f - t
                val radius = 20f + 120f * t

                drawCircle(
                    color = theme.lightningGlow.copy(alpha = 0.28f * fade),
                    radius = radius,
                    center = ring.center,
                    style = Stroke(width = 5f * fade + 1f),
                    blendMode = BlendMode.Screen
                )

                drawCircle(
                    color = theme.lightningCore.copy(alpha = 0.18f * fade),
                    radius = radius * 0.92f,
                    center = ring.center,
                    style = Stroke(width = 2f * fade + 0.5f),
                    blendMode = BlendMode.Screen
                )
            }

            plasma?.let { state ->
                drawElectricCorona(
                    center = state.position,
                    coreColor = theme.plasmaCore,
                    glowColor = theme.lightningGlow,
                    timeMs = now - state.bornAtMs,
                    showAnchorRing = config.plasmaAnchorRingEnabled
                )
            }

            bolts.forEach { bolt ->
                val age = (now - bolt.bornAtMs).coerceAtLeast(0)
                val t = (age / bolt.lifeMs.toFloat()).coerceIn(0f, 1f)
                val fade = 1f - t

                flashAccumulator += bolt.flashStrength * fade

                drawLightningPath(
                    points = bolt.points,
                    coreColor = theme.lightningCore.copy(alpha = bolt.alpha * fade),
                    glowColor = theme.lightningGlow.copy(alpha = 0.42f * fade),
                    strokeWidth = bolt.strokeWidth,
                    glowWidth = bolt.glowWidth
                )

                bolt.branches.forEach { branch ->
                    drawLightningPath(
                        points = branch,
                        coreColor = theme.lightningCore.copy(alpha = 0.56f * fade),
                        glowColor = theme.lightningGlow.copy(alpha = 0.20f * fade),
                        strokeWidth = max(0.9f, bolt.strokeWidth * 0.62f),
                        glowWidth = max(2.5f, bolt.glowWidth * 0.52f)
                    )
                }
            }

            if (config.flashEnabled && flashAccumulator > 0f) {
                val alpha = min(config.flashMaxAlpha, flashAccumulator * config.flashMaxAlpha)
                drawRect(
                    color = theme.flashColor.copy(alpha = alpha),
                    size = size,
                    blendMode = BlendMode.Screen
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(
                    config.enabled,
                    config.touchLaunchEnabled,
                    config.longPressPlasmaEnabled
                ) {
                    if (!config.enabled) return@pointerInput

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val pointerId: PointerId = down.id
                        val startTime = down.uptimeMillis

                        if (config.touchLaunchEnabled) {
                            spawnTouchBolt(down.position)
                        }

                        var longPressActivated = false
                        var latestPosition = down.position

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (change.positionChanged()) {
                                latestPosition = change.position
                            }

                            val elapsed = change.uptimeMillis - startTime
                            if (
                                config.longPressPlasmaEnabled &&
                                !longPressActivated &&
                                elapsed >= viewConfig.longPressTimeoutMillis
                            ) {
                                longPressActivated = true
                                plasma = PlasmaState(pointerId, latestPosition, timeMs)
                                spawnShockRing(latestPosition)
                            }

                            if (longPressActivated) {
                                plasma = plasma?.copy(position = latestPosition)
                            }

                            if (change.changedToUpIgnoreConsumed()) {
                                if (longPressActivated) {
                                    plasma = null
                                }
                                break
                            }
                        }

                        if (plasma?.pointerId == pointerId) {
                            plasma = null
                        }
                    }
                }
        )

        Box(modifier = Modifier.matchParentSize()) {
            content()
        }
    }
}