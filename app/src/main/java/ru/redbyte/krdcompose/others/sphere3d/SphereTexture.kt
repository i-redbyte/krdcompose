package ru.redbyte.krdcompose.others.sphere3d

import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap

data class Float3(val x: Float, val y: Float, val z: Float)

fun Float3.normalized(): Float3 {
    val len = kotlin.math.sqrt(x * x + y * y + z * z)
    if (len == 0f) return this
    return Float3(x / len, y / len, z / len)
}

data class TextureInfo(
    val shader: Shader,
    val width: Float,
    val height: Float,
)

fun createSphereTexture(
    useImage: Boolean,
    image: ImageBitmap?,
    useText: Boolean,
    text: String,
    textColor: Color,
    textSizePx: Float,
    sizePx: Int = 512,
): TextureInfo? {
    if (!useImage && !useText) return null

    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)

    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    if (useImage && image != null) {
        val src = image.asAndroidBitmap()
        val srcRect = Rect(0, 0, src.width, src.height)
        val inset = (sizePx * 0.2f).toInt()
        val dstRect = Rect(inset, inset, sizePx - inset, sizePx - inset)
        canvas.drawBitmap(src, srcRect, dstRect, paint)
    }

    if (useText && text.isNotBlank()) {
        paint.apply {
            color = textColor.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = textSizePx
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val x = sizePx / 2f
        val y = sizePx / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, x, y, paint)
    }

    val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

    return TextureInfo(
        shader = bitmapShader,
        width = sizePx.toFloat(),
        height = sizePx.toFloat(),
    )
}