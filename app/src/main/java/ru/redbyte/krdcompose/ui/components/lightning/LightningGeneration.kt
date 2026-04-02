package ru.redbyte.krdcompose.ui.components.lightning

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

internal fun generateLightningPath(
    start: Offset,
    end: Offset,
    random: Random,
    jitter: Float,
    minSegments: Int,
    maxSegments: Int
): List<Offset> {
    val segments = random.nextInt(minSegments, maxSegments + 1)
    val dir = end - start
    val length = dir.getDistance().coerceAtLeast(1f)
    val normal = Offset(-dir.y / length, dir.x / length)

    val points = ArrayList<Offset>(segments + 1)
    points += start

    for (i in 1 until segments) {
        val t = i / segments.toFloat()
        val base = lerp(start, end, t)
        val envelope = sin(t * PI).toFloat()
        val offsetAmount = signedRandom(random) * jitter * envelope
        val forwardJitter = signedRandom(random) * jitter * 0.15f
        points += base + normal * offsetAmount + dir.normalized() * forwardJitter
    }

    points += end
    return points
}

internal fun generateBranches(
    main: List<Offset>,
    random: Random,
    countRange: IntRange,
    maxLenRatio: Float
): List<List<Offset>> {
    if (main.size < 4) return emptyList()

    val count = random.nextInt(countRange.first, countRange.last + 1)
    val out = mutableListOf<List<Offset>>()

    repeat(count) {
        val index = random.nextInt(1, main.size - 2)
        val start = main[index]
        val next = main[index + 1]
        val baseDir = (next - start).normalized()
        val angleOffset =
            random.nextDouble(28.0, 84.0).toFloat() * if (random.nextBoolean()) 1f else -1f
        val branchDir = rotate(baseDir, angleOffset)

        val mainLength = (main.last() - main.first()).getDistance()
        val branchLength = mainLength * random.nextDouble(0.10, maxLenRatio.toDouble()).toFloat()
        val end = start + branchDir * branchLength

        out += generateLightningPath(
            start = start,
            end = end,
            random = random,
            jitter = 9f,
            minSegments = 4,
            maxSegments = 7
        )
    }

    return out
}