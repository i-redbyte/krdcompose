package ru.redbyte.krdcompose.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

class TrailPathCache(
    private val maxPoints: Int,
    private val minSegmentWorld: Float
) {
    private val points = ArrayDeque<Offset>()
    private val path = Path()

    private var lastBuiltCount = 0
    private var lastScale = Float.NaN
    private var lastCx = Float.NaN
    private var lastCy = Float.NaN

    private var lastWorldPoint: Offset? = null

    fun clear() {
        points.clear()
        path.reset()
        lastBuiltCount = 0
        lastWorldPoint = null
    }

    fun tryAdd(world: Offset) {
        val prev = lastWorldPoint
        if (prev == null || (world - prev).getDistance() >= minSegmentWorld) {
            points.addLast(world)
            if (points.size > maxPoints) points.removeFirst()
            lastWorldPoint = world
        }
    }

    fun buildIfNeeded(scale: Float, cx: Float, cy: Float) {
        val needRebuild = (points.size != lastBuiltCount) ||
                scale != lastScale || cx != lastCx || cy != lastCy
        if (!needRebuild) return

        path.reset()
        val it = points.iterator()
        if (!it.hasNext()) {
            lastBuiltCount = points.size
            lastScale = scale
            lastCx = cx
            lastCy = cy
            return
        }
        val first = it.next()
        path.moveTo(cx + first.x * scale, cy - first.y * scale)
        while (it.hasNext()) {
            val p = it.next()
            path.lineTo(cx + p.x * scale, cy - p.y * scale)
        }
        lastBuiltCount = points.size
        lastScale = scale
        lastCx = cx
        lastCy = cy
    }

    fun draw(draw: androidx.compose.ui.graphics.drawscope.DrawScope, color: Color) {
        if (lastBuiltCount > 1) {
            draw.drawPath(path, color = color, style = Stroke(width = 1.5f))
        }
    }
}

operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)
