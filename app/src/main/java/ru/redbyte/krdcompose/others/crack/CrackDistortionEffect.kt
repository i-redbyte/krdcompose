package ru.redbyte.krdcompose.others.crack

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import ru.redbyte.krdcompose.R

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.crackDistortionEffect(
    seeds: List<CrackSeed>,
    strengthPx: Float = 3.5f,
    radiusPx: Float = 180f
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val shader = rememberRuntimeShaderFromRaw(R.raw.crack_distort)

    val active = seeds.takeLast(CRACK_MAX_SEEDS)
    val seedCount = active.size
    val seedPos = remember { FloatArray(CRACK_MAX_SEEDS * 2) }
    val seedSalt = remember { FloatArray(CRACK_MAX_SEEDS) }

    active.forEachIndexed { i, s ->
        seedPos[i * 2] = s.x
        seedPos[i * 2 + 1] = s.y
        seedSalt[i] = s.salt
    }

    this
        .onSizeChanged { size = it }
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen

            if (size.width > 0 && size.height > 0 && seedCount > 0) {
                shader.setFloatUniform(
                    "uResolution",
                    size.width.toFloat(),
                    size.height.toFloat()
                )
                shader.setFloatUniform(
                    "uStrength",
                    strengthPx.coerceAtLeast(0f)
                )
                shader.setFloatUniform(
                    "uRadius",
                    radiusPx.coerceAtLeast(1f)
                )
                shader.setFloatUniform(
                    "uSeedPos",
                    seedPos
                )
                shader.setFloatUniform(
                    "uSeedSalt",
                    seedSalt
                )
                shader.setFloatUniform(
                    "uSeedCount",
                    seedCount.toFloat()
                )

                renderEffect = RenderEffect
                    .createRuntimeShaderEffect(shader, "content")
                    .asComposeRenderEffect()
            } else {
                renderEffect = null
            }
        }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun rememberRuntimeShaderFromRaw(@RawRes resId: Int): RuntimeShader {
    val context = LocalContext.current
    val source = remember(resId) {
        context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    }
    return remember(source) {
        try {
            RuntimeShader(source)
        } catch (e: IllegalArgumentException) {
            Log.e("CrackShader", "AGSL compile failed", e)
            null
        } as RuntimeShader
    }
}
