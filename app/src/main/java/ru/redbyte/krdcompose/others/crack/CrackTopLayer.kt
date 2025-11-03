package ru.redbyte.krdcompose.others.crack

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

@Stable
class CrackState(
    initial: List<CrackSeed> = emptyList(),
    private val maxSeeds: Int = CRACK_MAX_SEEDS
) {
    private val _seeds = mutableStateListOf<CrackSeed>().also { it.addAll(initial) }
    val seeds: List<CrackSeed> get() = _seeds

    fun addOrBoost(at: Offset) {
        val hitThresholdPx = 56f
        var nearestIndex = -1
        var nearestDist = Float.MAX_VALUE
        _seeds.forEachIndexed { idx, s ->
            val d = (s.x - at.x).pow(2) + (s.y - at.y).pow(2)
            if (d < nearestDist) {
                nearestDist = d; nearestIndex = idx
            }
        }
        val hit = sqrt(nearestDist) <= hitThresholdPx

        if (hit && nearestIndex >= 0) {
            val s = _seeds[nearestIndex]
            _seeds[nearestIndex] = s.copy(
                power = (s.power + 0.18f).coerceAtMost(1.0f),
                branchBoost = (s.branchBoost + 0.6f).coerceAtMost(6f)
            )
        } else {
            if (_seeds.size >= maxSeeds) _seeds.removeAt(0)
            _seeds.add(CrackSeed(x = at.x, y = at.y, power = 0.6f, branchBoost = 0f))
        }
    }

}

@Composable
fun rememberCrackState(
    initial: List<CrackSeed> = emptyList(),
    maxSeeds: Int = CRACK_MAX_SEEDS
): CrackState = remember { CrackState(initial, maxSeeds) }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CrackRoot(
    modifier: Modifier = Modifier,
    state: CrackState = rememberCrackState(),
    distortion: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.fillMaxSize()) {

        val distortMod = if (distortion) {
            Modifier.crackDistortionEffect(
                seeds = state.seeds,
                strengthPx = 8f,
                radiusPx = 260f
            )
        } else Modifier

        Box(Modifier
            .matchParentSize()
            .then(distortMod)) {
            content()
        }

        Box(
            Modifier
                .matchParentSize()
                .pointerInput(state) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        state.addOrBoost(down.position)
                    }
                }
                .crackedEffect(
                    seeds = state.seeds,
                    thicknessDp = 3.2.dp,
                    jitterPx = 14f,
                    haloStrength = 0.30f,
                    safetyOverlay = false,
                    debugSegments = false
                )
        )
    }
}
