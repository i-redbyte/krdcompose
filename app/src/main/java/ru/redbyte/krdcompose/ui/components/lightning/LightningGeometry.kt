package ru.redbyte.krdcompose.ui.components.lightning

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

internal fun rayToBounds(start: Offset, direction: Offset, size: Size): Offset {
    val dx = if (abs(direction.x) < 0.0001f) 0.0001f else direction.x
    val dy = if (abs(direction.y) < 0.0001f) 0.0001f else direction.y

    val tx = if (dx > 0) (size.width - start.x) / dx else (0f - start.x) / dx
    val ty = if (dy > 0) (size.height - start.y) / dy else (0f - start.y) / dy

    val t = minPositive(tx, ty).coerceAtLeast(0f)
    return Offset(start.x + dx * t, start.y + dy * t)
}

internal fun minPositive(a: Float, b: Float): Float {
    val pa = if (a > 0f) a else Float.POSITIVE_INFINITY
    val pb = if (b > 0f) b else Float.POSITIVE_INFINITY
    return min(pa, pb)
}

internal fun clampToBounds(offset: Offset, size: Size): Offset =
    Offset(
        x = offset.x.coerceIn(0f, size.width),
        y = offset.y.coerceIn(0f, size.height)
    )

internal fun angleToUnitVector(degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    return Offset(cos(rad).toFloat(), sin(rad).toFloat()).normalized()
}

internal fun rotate(v: Offset, degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    val c = cos(rad).toFloat()
    val s = sin(rad).toFloat()
    return Offset(
        x = v.x * c - v.y * s,
        y = v.x * s + v.y * c
    ).normalized()
}

internal fun lerp(a: Offset, b: Offset, t: Float): Offset =
    Offset(
        x = a.x + (b.x - a.x) * t,
        y = a.y + (b.y - a.y) * t
    )

internal fun Offset.normalized(): Offset {
    val len = getDistance()
    return if (len <= 0.0001f) Offset.Zero else Offset(x / len, y / len)
}

internal fun signedRandom(random: Random): Float =
    random.nextFloat() * if (random.nextBoolean()) 1f else -1f

internal operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)
internal operator fun Offset.minus(other: Offset): Offset = Offset(x - other.x, y - other.y)
internal operator fun Offset.times(value: Float): Offset = Offset(x * value, y * value)

internal val FullCircleRadians = (PI * 2f).toFloat()