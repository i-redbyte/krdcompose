package ru.redbyte.krdcompose.ui.components.lightning

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin

internal fun DrawScope.drawLightningPath(
    points: List<Offset>,
    coreColor: Color,
    glowColor: Color,
    strokeWidth: Float,
    glowWidth: Float
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    drawPath(path = path, color = glowColor, style = Stroke(width = glowWidth))
    drawPath(
        path = path,
        color = coreColor.copy(alpha = coreColor.alpha * 0.50f),
        style = Stroke(width = strokeWidth * 2.0f)
    )
    drawPath(path = path, color = coreColor, style = Stroke(width = strokeWidth))
}

internal fun DrawScope.drawElectricCorona(
    center: Offset,
    coreColor: Color,
    glowColor: Color,
    timeMs: Long,
    showAnchorRing: Boolean
) {
    val phase = (timeMs % 1200L) / 1200f
    val pulse = 0.82f + 0.18f * sin(phase * FullCircleRadians)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.14f * pulse),
                glowColor.copy(alpha = 0.07f * pulse),
                Color.Transparent
            ),
            center = center,
            radius = 96f
        ),
        radius = 96f,
        center = center,
        blendMode = BlendMode.Screen
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.44f * pulse),
                coreColor.copy(alpha = 0.24f * pulse),
                Color.Transparent
            ),
            center = center,
            radius = 28f
        ),
        radius = 28f,
        center = center,
        blendMode = BlendMode.Screen
    )

    if (showAnchorRing) {
        drawCircle(
            color = glowColor.copy(alpha = 0.22f * pulse),
            radius = 24f + 4f * pulse,
            center = center,
            style = Stroke(width = 1.6f),
            blendMode = BlendMode.Screen
        )
    }

    repeat(18) { i ->
        val a = (i / 18f) * 360f + phase * 120f
        val dir = angleToUnitVector(a)
        val inner = center + dir * 18f
        val outer =
            center + dir * (26f + (sin((phase * 360f + i * 24f) * Math.PI / 180f).toFloat() * 6f + 6f))

        drawLine(
            color = glowColor.copy(alpha = 0.30f * pulse),
            start = inner,
            end = outer,
            strokeWidth = 1.2f
        )
    }
}