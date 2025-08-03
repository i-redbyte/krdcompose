package ru.redbyte.krdcompose.games.hanoiTowers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class AnimatedRing(
    val size: Int,
    val color: Color,
    val anim: Animatable<Offset, AnimationVector2D>,
    val widthPx: Float,
    val heightPx: Float
)
