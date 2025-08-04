package ru.redbyte.krdcompose.games.hanoiTowers

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Игровой компонент «Башни Ханоя» с поддержкой автоматического решения.
 *
 * Для **3 стержней** используется классический алгоритм (2^n−1 ходов).
 * Для **4 и более** реализована простая, но быстрая реализация алгоритма
 * Frame‑Stewart с эвристикой \(k = n - ⌈√{2n}⌉ + 1\).
 * Он не гарантирует строго минимальное число ходов, но ощутимо
 * эффективнее «жадного» способа и демонстрирует преимущество дополнительных стержней.
 *
 * @param rods              Количество стержней (≥3)
 * @param rings             Количество колец  (≥1)
 * @param ringColors        Палитра цветов — переиспользуется циклически
 * @param onVictory         Коллбек «победа». Возвращает число совершённых ходов
 * @param autoPlayTrigger   Любое изменение значения запускает авто‑игру.
 *                          Используйте простой счётчик в родительском composable: trigger++
 * @param onAutoPlayFinish  Коллбек по окончании авто‑игры (после onVictory)
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TowerOfHanoiGame(
    rods: Int,
    rings: Int,
    ringColors: List<Color>,
    onVictory: (Int) -> Unit,
    autoPlayTrigger: Int = 0,
    onAutoPlayFinish: () -> Unit = {}
) {
    val game = remember { HanoiGame(rods, rings) }
    var selectedRod by remember { mutableStateOf<Int?>(null) }
    val animRing = remember { mutableStateOf<AnimatedRing?>(null) }
    val scope = rememberCoroutineScope()
    var isAutoPlaying by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val ringHeight = 24.dp
        val ringSpacing = 4.dp
        val ringHeightPx = with(density) { ringHeight.toPx() }
        val spacingPx = with(density) { ringSpacing.toPx() }
        val widthPerRodPx = with(density) { (maxWidth / rods).toPx() }
        val stackHeightPx = rings * ringHeightPx + (rings - 1) * spacingPx
        val towerAreaHeightPx = stackHeightPx * 1.2f
        val towerAreaHeight = with(density) { towerAreaHeightPx.toDp() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val verticalOffsetPx = ((maxHeightPx - towerAreaHeightPx) / 2f)
            .coerceAtLeast(0f)

        suspend fun animateMove(from: Int, to: Int) {
            val startTowerSize = game.towers[from].size
            val endTowerSizeBeforeMove = game.towers[to].size
            val ring = game.popRing(from)
            val fraction = if (rings == 1) 1f else 0.3f + (ring - 1f) / (rings - 1) * 0.7f

            val startX = widthPerRodPx * (from + 0.5f)
            val startY = verticalOffsetPx +
                    towerAreaHeightPx - (ringHeightPx + spacingPx) * startTowerSize + ringHeightPx / 2f

            val endX = widthPerRodPx * (to + 0.5f)
            val endY = verticalOffsetPx +
                    towerAreaHeightPx - (ringHeightPx + spacingPx) * (endTowerSizeBeforeMove + 1) + ringHeightPx / 2f

            val liftY = (verticalOffsetPx + towerAreaHeightPx - stackHeightPx) - ringHeightPx * 2

            val anim = Animatable(Offset(startX, startY), Offset.VectorConverter)
            val color = ringColors[(ring - 1) % ringColors.size]
            animRing.value = AnimatedRing(ring, color, anim, widthPerRodPx * fraction, ringHeightPx)

            anim.animateTo(Offset(startX, liftY), tween(200, easing = LinearEasing))
            anim.animateTo(Offset(endX, liftY), tween(400, easing = LinearEasing))
            anim.animateTo(Offset(endX, endY), tween(200, easing = LinearEasing))

            game.pushRing(to, ring)
            animRing.value = null
            selectedRod = null
            if (game.isVictory()) onVictory(game.moves)
        }

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(towerAreaHeight)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 16.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                repeat(rods) { index ->
                    val tower = game.towers[index]

                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(enabled = animRing.value == null && !isAutoPlaying) {
                                if (selectedRod == null) {
                                    if (tower.isNotEmpty()) selectedRod = index
                                } else {
                                    val from = selectedRod!!
                                    val to = index
                                    if (from == to) {
                                        selectedRod = null; return@clickable
                                    }
                                    if (game.canMove(from, to)) {
                                        scope.launch { animateMove(from, to) }
                                    } else {
                                        selectedRod = null
                                    }
                                }
                            }
                    ) {
                        CanvasRod()
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            tower.asReversed().forEachIndexed { i, ring ->
                                RingView(ring, rings, ringColors, ringHeight)
                                if (i != tower.lastIndex) {
                                    Spacer(Modifier.height(ringSpacing))
                                }
                            }
                        }
                    }
                }
            }
        }
        animRing.value?.let { overlay ->
            val offset = overlay.anim.value
            Box(
                Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .size(
                        with(density) { overlay.widthPx.toDp() },
                        with(density) { overlay.heightPx.toDp() }
                    )
                    .graphicsLayer { rotationZ = 180f }
                    .background(overlay.color, RoundedCornerShape(8.dp))
            )
        }
        LaunchedEffect(autoPlayTrigger) {
            if (autoPlayTrigger == 0) return@LaunchedEffect
            if (isAutoPlaying) return@LaunchedEffect
            if (game.isVictory()) {
                onAutoPlayFinish(); return@LaunchedEffect
            }

            isAutoPlaying = true

            val moves = mutableListOf<Pair<Int, Int>>()

            fun gen3(n: Int, from: Int, to: Int, aux: Int) {
                if (n == 0) return
                gen3(n - 1, from, aux, to)
                moves += from to to
                gen3(n - 1, aux, to, from)
            }

            fun frameStewart(n: Int, from: Int, to: Int, auxRods: List<Int>) {
                if (n == 0) return
                val r = auxRods.size + 2
                if (r == 3) {
                    gen3(n, from, to, auxRods.first())
                    return
                }
                if (n == 1) {
                    moves += from to to; return
                }

                // k = n - ⌈√(2n)⌉ + 1
                val k = (n - sqrt((2 * n).toDouble()).roundToInt() + 1)
                    .coerceIn(1, n - 1)

                val firstAux = auxRods.first()
                val restAux = auxRods.drop(1)
                frameStewart(k, from, firstAux, listOf(to) + restAux)
                frameStewart(n - k, from, to, restAux)
                frameStewart(k, firstAux, to, listOf(from) + restAux)
            }

            if (rods == 3) {
                gen3(rings, 0, 1, 2)
            } else {
                val auxList = (2 until rods).toList()
                frameStewart(rings, 0, 1, auxList)
            }
            for ((from, to) in moves) {
                animateMove(from, to)
            }

            isAutoPlaying = false
            onAutoPlayFinish()
        }
    }
}

@Composable
fun CanvasRod() {
    Canvas(Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val rodW = 4.dp.toPx()
        drawRect(
            Color.DarkGray,
            topLeft = Offset(centerX - rodW / 2f, 0f),
            size = Size(rodW, size.height)
        )
    }
}

@Composable
fun RingView(ring: Int, total: Int, colors: List<Color>, h: Dp) {
    val frac = if (total == 1) 1f else 0.3f + (ring - 1f) / (total - 1) * 0.7f
    Box(
        Modifier
            .fillMaxWidth(frac)
            .height(h)
            .graphicsLayer { rotationZ = 180f }
            .background(
                color = colors[(ring - 1) % colors.size],
                shape = RoundedCornerShape(8.dp)
            )
    )
}
