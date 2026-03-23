package ru.redbyte.krdcompose.ui.components.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ru.redbyte.krdcompose.screens.list.hackerList.HackerListStyle
import ru.redbyte.krdcompose.screens.list.hackerList.ScanLineMode
import ru.redbyte.krdcompose.screens.list.hackerList.cardShape

@Composable
fun ScanHighlight(
    scanTrigger: Int,
    selected: Boolean,
    style: HackerListStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = style.cardShape()
    val palette = style.palette
    val dimensions = style.dimensions
    val animations = style.animations

    val progress = remember { Animatable(0f) }
    val visible = remember { Animatable(0f) }

    LaunchedEffect(scanTrigger) {
        if (scanTrigger <= 0) return@LaunchedEffect

        visible.snapTo(1f)
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animations.scanDurationMillis,
                easing = LinearEasing
            )
        )
        visible.snapTo(0f)
        progress.snapTo(0f)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (style.scanLineMode == ScanLineMode.Clipped) {
                    Modifier.clip(shape)
                } else {
                    Modifier
                }
            )
            .border(
                width = dimensions.cardBorderWidth,
                color = when {
                    selected && style.showSelectedBorderGlow -> palette.cardSelectedBorder
                    else -> palette.cardBorder
                },
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    if (visible.value > 0f) {
                        val y = size.height * progress.value
                        val bandHalfHeight = size.height * dimensions.scanBandHeightFraction
                        val glow = palette.scanGlow.copy(alpha = visible.value)
                        val core = palette.scanCore.copy(alpha = visible.value)

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    glow,
                                    core,
                                    glow,
                                    Color.Transparent
                                ),
                                startY = y - bandHalfHeight,
                                endY = y + bandHalfHeight
                            ),
                            blendMode = BlendMode.Screen
                        )

                        drawLine(
                            color = core,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = dimensions.scanLineThickness.toPx()
                        )
                    }
                }
        ) {
            content()
        }
    }
}