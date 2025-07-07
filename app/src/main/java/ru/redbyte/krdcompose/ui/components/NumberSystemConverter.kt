package ru.redbyte.krdcompose.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberSystemConverter(
    modifier: Modifier = Modifier,
    maxDigits: Int = 16,
    cornerRadius: Dp = 16.dp,
    bitColorScheme: BitColorScheme = BitColorScheme.GROUP_8,
    verticalLayout: Boolean = false,
    decimalFieldHint: String = "десятичное",
    hexFieldHint: String = "шестнадцатеричное",
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    var decInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }
    var hexInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }
    var editingSource by remember { mutableStateOf(Source.NONE) }

    val sanitizedDec = remember(decInput.text) { decInput.text.filter { it.isDigit() }.take(20) }
    val decValue = sanitizedDec.toULongOrNull()
    val derivedHex = remember(decValue) { decValue?.toString(16)?.uppercase() ?: "" }

    val sanitizedHex = remember(hexInput.text) {
        hexInput.text.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
            .uppercase()
            .take(maxDigits)
    }
    val hexValue = sanitizedHex.toULongOrNull(16)
    val derivedDec = remember(hexValue) { hexValue?.toString() ?: "" }

    LaunchedEffect(sanitizedDec, sanitizedHex, editingSource) {
        when (editingSource) {
            Source.DECIMAL -> {
                if (sanitizedDec != decInput.text) decInput = decInput.copy(text = sanitizedDec)
                if (derivedHex != hexInput.text) hexInput = hexInput.copy(text = derivedHex)
            }

            Source.HEX -> {
                if (sanitizedHex != hexInput.text) hexInput = hexInput.copy(text = sanitizedHex)
                if (derivedDec != decInput.text) decInput = decInput.copy(text = derivedDec)
            }

            else -> Unit
        }
    }
    Column(modifier) {
        @Composable
        fun DecimalField() = BaseField(
            value = decInput,
            onValueChange = { editingSource = Source.DECIMAL; decInput = it },
            placeholder = decimalFieldHint,
            cornerRadius = cornerRadius,
            modifier = if (verticalLayout) Modifier.fillMaxWidth() else Modifier.weight(1f)
        )

        @Composable
        fun HexField() = BaseField(
            value = hexInput,
            onValueChange = { editingSource = Source.HEX; hexInput = it },
            placeholder = hexFieldHint,
            cornerRadius = cornerRadius,
            modifier = if (verticalLayout) Modifier.fillMaxWidth() else Modifier.weight(1f)
        )

        if (verticalLayout) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                DecimalField()
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .size(20.dp)
                )
                HexField()
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DecimalField()
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(20.dp)
                )
                HexField()
            }
        }

        Spacer(Modifier.height(12.dp))

        BitRuler(
            value = decValue ?: hexValue ?: 0u,
            scheme = bitColorScheme,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        )
    }
}

@Composable
private fun BaseField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val fontSize = when (value.text.length) {
        in 0..10 -> 18.sp
        in 11..16 -> 16.sp
        else -> 14.sp
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        textStyle = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Medium),
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun BitRuler(
    value: ULong,
    scheme: BitColorScheme,
    modifier: Modifier = Modifier,
) {
    val bits = remember(value) { List(64) { ((value shr (63 - it)) and 1uL) == 1uL } }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val widthPerBit = canvasWidth / 64f

        fun xForBit(i: Int) = (i + 0.5f) * widthPerBit

        val bitTextY = size.height * 0.18f
        val longTickStart = size.height * 0.28f
        val shortTickStart = size.height * 0.36f
        val baseLineY = size.height * 0.58f
        val indexTextY = size.height * 0.88f

        val bitTextSize = 12.sp.toPx()
        val indexTextSize = 10.sp.toPx()

        fun colourFor(idx: Int): Color = when (scheme) {
            BitColorScheme.SINGLE -> Color(0xFF3F51B5)
            BitColorScheme.GROUP_32 -> if (idx < 32) Color(0xFF009688) else Color(0xFFFF5722)
            BitColorScheme.GROUP_16 -> when (idx / 16) {
                0 -> Color(0xFF9C27B0)
                1 -> Color(0xFF03A9F4)
                2 -> Color(0xFFFFC107)
                else -> Color(0xFFF44336)
            }

            BitColorScheme.GROUP_8 -> when ((idx / 8) % 8) {
                0 -> Color(0xFFEF5350)
                1 -> Color(0xFFAB47BC)
                2 -> Color(0xFF5C6BC0)
                3 -> Color(0xFF29B6F6)
                4 -> Color(0xFF66BB6A)
                5 -> Color(0xFFFFEB3B)
                6 -> Color(0xFFFFA726)
                else -> Color(0xFF8D6E63)
            }
        }

        repeat(64) { i ->
            val x = xForBit(i)
            val isGroupTick = i % 8 == 0 || i == 63
            val tickStart = if (isGroupTick) longTickStart else shortTickStart
            val colour = colourFor(i)

            drawLine(colour, Offset(x, tickStart), Offset(x, baseLineY), strokeWidth = 1f)

            drawContext.canvas.nativeCanvas.apply {
                val paintBit = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = bitTextSize
                    color = colour.toArgb()
                    isAntiAlias = true
                }
                drawText(if (bits[i]) "1" else "0", x, bitTextY, paintBit)
            }

            val position = 64 - i
            drawContext.canvas.nativeCanvas.apply {
                val paintIdx = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = indexTextSize
                    color = android.graphics.Color.DKGRAY
                    isAntiAlias = true
                    isFakeBoldText = position % 8 == 0
                }
                if (position in listOf(64, 32, 16, 8, 1))
                    drawText(position.toString(), x, indexTextY, paintIdx)
            }
        }
    }
}

enum class BitColorScheme { SINGLE, GROUP_32, GROUP_16, GROUP_8 }
private enum class Source { DECIMAL, HEX, NONE }
