package ru.redbyte.krdcompose.screens.list.hackerList

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ScanLineMode {
    Overlay,
    Clipped
}

@Immutable
data class HackerPalette(
    val screenBackground: Color,
    val cardBackground: Color,
    val cardSelectedBackground: Color,
    val cardBorder: Color,
    val cardSelectedBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val accentDim: Color,
    val scanGlow: Color,
    val scanCore: Color,
    val warning: Color,
    val danger: Color,
    val decodedPanelBackground: Color
)

@Immutable
data class HackerAnimationSpec(
    val scanDurationMillis: Int = 900,
    val decodeStepDelayMillis: Long = 70L,
    val decodeSteps: Int = 12,
    val decodedTextRevealDelayMillis: Long = 900L,
    val cardColorDurationMillis: Int = 220,
    val contentFadeDurationMillis: Int = 220
)

@Immutable
data class HackerDimensions(
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 20.dp,
    val itemSpacing: Dp = 14.dp,
    val cardCornerRadius: Dp = 18.dp,
    val innerCornerRadius: Dp = 14.dp,
    val cardBorderWidth: Dp = 1.dp,
    val scanLineThickness: Dp = 2.dp,
    val scanBandHeightFraction: Float = 0.16f
)

@Immutable
data class HackerListStyle(
    val palette: HackerPalette,
    val animations: HackerAnimationSpec = HackerAnimationSpec(),
    val dimensions: HackerDimensions = HackerDimensions(),
    val scanLineMode: ScanLineMode = ScanLineMode.Clipped,
    val showSelectedBorderGlow: Boolean = true
)

object HackerListStyles {

    val MatrixGreen = HackerListStyle(
        palette = HackerPalette(
            screenBackground = Color(0xFF081018),
            cardBackground = Color(0xFF0E1722),
            cardSelectedBackground = Color(0xFF122231),
            cardBorder = Color(0x6600F7A5),
            cardSelectedBorder = Color(0x9900F7A5),
            primaryText = Color(0xFFD7FBEF),
            secondaryText = Color(0xFF85B5A7),
            accent = Color(0xFF00F7A5),
            accentDim = Color(0x6600F7A5),
            scanGlow = Color(0x9900F7A5),
            scanCore = Color(0xFFB6FFE4),
            warning = Color(0xFFFFC857),
            danger = Color(0xFFFF5370),
            decodedPanelBackground = Color(0x66131F2A)
        ),
        scanLineMode = ScanLineMode.Clipped
    )

    val CyanBreach = HackerListStyle(
        palette = HackerPalette(
            screenBackground = Color(0xFF091019),
            cardBackground = Color(0xFF0D1823),
            cardSelectedBackground = Color(0xFF12283A),
            cardBorder = Color(0x6632DFFF),
            cardSelectedBorder = Color(0x9932DFFF),
            primaryText = Color(0xFFE1F8FF),
            secondaryText = Color(0xFF8EBBC7),
            accent = Color(0xFF32DFFF),
            accentDim = Color(0x6632DFFF),
            scanGlow = Color(0x9932DFFF),
            scanCore = Color(0xFFC5F6FF),
            warning = Color(0xFFFFC857),
            danger = Color(0xFFFF5A7A),
            decodedPanelBackground = Color(0x6613212B)
        ),
        scanLineMode = ScanLineMode.Clipped
    )

    val RedAlert = HackerListStyle(
        palette = HackerPalette(
            screenBackground = Color(0xFF120A0D),
            cardBackground = Color(0xFF1B1014),
            cardSelectedBackground = Color(0xFF2A151A),
            cardBorder = Color(0x66FF5370),
            cardSelectedBorder = Color(0x99FF5370),
            primaryText = Color(0xFFFFE3E8),
            secondaryText = Color(0xFFDAA6B0),
            accent = Color(0xFFFF5370),
            accentDim = Color(0x66FF5370),
            scanGlow = Color(0x99FF5370),
            scanCore = Color(0xFFFFC0CB),
            warning = Color(0xFFFFC857),
            danger = Color(0xFFFF5370),
            decodedPanelBackground = Color(0x66251519)
        ),
        scanLineMode = ScanLineMode.Clipped
    )
}

fun HackerListStyle.cardShape(): RoundedCornerShape {
    return RoundedCornerShape(dimensions.cardCornerRadius)
}

fun HackerListStyle.innerShape(): RoundedCornerShape {
    return RoundedCornerShape(dimensions.innerCornerRadius)
}