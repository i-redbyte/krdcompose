package ru.redbyte.krdcompose.screens.kepler

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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.redbyte.krdcompose.others.kepler.model.BoundaryMode
import ru.redbyte.krdcompose.others.kepler.model.ColorsConfig
import ru.redbyte.krdcompose.others.kepler.model.OrbitParams
import ru.redbyte.krdcompose.others.kepler.model.OrbitType
import ru.redbyte.krdcompose.others.kepler.model.RectWorld
import ru.redbyte.krdcompose.others.kepler.model.VisualConfig
import ru.redbyte.krdcompose.others.kepler.model.WorldConstraints
import ru.redbyte.krdcompose.utils.TrailPathCache
import kotlin.math.*

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
