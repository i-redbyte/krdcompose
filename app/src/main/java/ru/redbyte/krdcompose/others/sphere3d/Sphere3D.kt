package ru.redbyte.krdcompose.others.sphere3d

import android.content.Context
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import ru.redbyte.krdcompose.R

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Sphere3D(
    modifier: Modifier = Modifier,
    sphereColor: Color = Color(0xFFFFC107),
    lightDirection: Float3 = Float3(0.3f, 0.6f, 1f),
    useImage: Boolean = false,
    image: ImageBitmap? = null,
    useText: Boolean = false,
    text: String = "",
    textColor: Color = Color.White,
    textSizeSp: Float = 16f,
    rotationX: Float,
    rotationY: Float,
    onRotateDelta: (dx: Float, dy: Float) -> Unit,
) {
    val density = LocalDensity.current

    val shader: RuntimeShader = rememberSphereShader()
    val brush = remember { ShaderBrush(shader) }

    val textureInfo = remember(
        useImage, image, useText, text, textColor, textSizeSp, density
    ) {
        createSphereTexture(
            useImage = useImage,
            image = image,
            useText = useText,
            text = text,
            textColor = textColor,
            textSizePx = with(density) { textSizeSp.sp.toPx() },
        )
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, drag ->
                change.consume()
                onRotateDelta(drag.x, drag.y)
            }
        }
    ) {
        val sz = size
        shader.setFloatUniform("iResolution", sz.width, sz.height)
        shader.setColorUniform("sphereColor", sphereColor.toArgb())
        shader.setFloatUniform("rotation", rotationX, rotationY)

        val lightNorm = lightDirection.normalized()
        shader.setFloatUniform("lightDir", lightNorm.x, lightNorm.y, lightNorm.z)

        if (textureInfo != null) {
            shader.setFloatUniform("useTexture", 1f)
            shader.setFloatUniform("textureSize", textureInfo.width, textureInfo.height)
            shader.setInputShader("texture", textureInfo.shader)
        } else {
            shader.setFloatUniform("useTexture", 0f)
            shader.setFloatUniform("textureSize", 1f, 1f)
        }

        drawRect(brush = brush, size = sz)
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createSphereShader(context: Context): RuntimeShader {
    val source = context.resources
        .openRawResource(R.raw.sphere_3d)
        .bufferedReader()
        .use { it.readText() }

    return RuntimeShader(source)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun rememberSphereShader(): RuntimeShader {
    val context = LocalContext.current
    return remember {
        createSphereShader(context)
    }
}

