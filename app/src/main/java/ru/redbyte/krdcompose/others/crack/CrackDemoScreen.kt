package ru.redbyte.krdcompose.others.crack

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun LegacyCrackScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Требуется Android 13+ (API 33) для AGSL.\nНа этой версии показана заглушка.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CrackDemoScreen() {
    val cracks = remember { mutableStateListOf<CrackSeed>() }
    val maxSeeds = CRACK_MAX_SEEDS

    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1020), Color(0xFF18223A), Color(0xFF0B1020))
                )
            )
            .crackedEffect(
                seeds = cracks,
                thicknessDp = 1.2.dp,            // было 3.dp
                jitterPx = 12f,
                crackColor = Color(0x66000000),  // чуть мягче тёмный вклад
                haloStrength = 0.35f,            // уже и слабее
                safetyOverlay = false,
                debugSegments = false
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val hitThresholdPx = 56.dp.toPx()
                        var nearestIndex = -1
                        var nearestDist = Float.MAX_VALUE
                        cracks.forEachIndexed { idx, seed ->
                            val d = (seed.x - offset.x).pow(2) + (seed.y - offset.y).pow(2)
                            if (d < nearestDist) {
                                nearestDist = d
                                nearestIndex = idx
                            }
                        }
                        val hit = sqrt(nearestDist) <= hitThresholdPx

                        if (hit && nearestIndex >= 0) {
                            val s = cracks[nearestIndex]
                            cracks[nearestIndex] = s.copy(
                                power = (s.power + 0.18f).coerceAtMost(1.0f),
                                branchBoost = (s.branchBoost + 0.6f).coerceAtMost(6f)
                            )
                        } else {
                            if (cracks.size >= maxSeeds) cracks.removeAt(0)
                            cracks.add(
                                CrackSeed(
                                    x = offset.x,
                                    y = offset.y,
                                    power = 0.6f,
                                    branchBoost = 0f
                                    // salt берётся по умолчанию (Random.nextFloat())
                                )
                            )
                        }
                    }
                )
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Жмите по экрану — появятся трещины.\nПовторный тап по существующей трещине — усилит её.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE6EAF5),
                textAlign = TextAlign.Center
            )
        }
    }
}
