package ru.redbyte.krdcompose.ui.components.lightning

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId

@Immutable
data class LightningTheme(
    val name: String,
    val background: Color,
    val panel: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val lightningCore: Color,
    val lightningGlow: Color,
    val plasmaCore: Color,
    val flashColor: Color
)

object LightningThemes {
    val BlueStorm = LightningTheme(
        name = "BlueStorm",
        background = Color(0xFF061019),
        panel = Color(0xAA0C1822),
        textPrimary = Color.White,
        textSecondary = Color(0xFFD7E8F7),
        lightningCore = Color(0xFFEAF7FF),
        lightningGlow = Color(0xFF63BFFF),
        plasmaCore = Color(0xFF79C8FF),
        flashColor = Color.White
    )

    val RedAlert = LightningTheme(
        name = "RedAlert",
        background = Color(0xFF180507),
        panel = Color(0xAA2A0E12),
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFD7D9),
        lightningCore = Color(0xFFFFF0F0),
        lightningGlow = Color(0xFFFF4D5E),
        plasmaCore = Color(0xFFFF7A85),
        flashColor = Color(0xFFFFD2D6)
    )

    val HackerGreen = LightningTheme(
        name = "HackerGreen",
        background = Color(0xFF031108),
        panel = Color(0xAA0A1A11),
        textPrimary = Color(0xFFE7FFE7),
        textSecondary = Color(0xFFB6F8BE),
        lightningCore = Color(0xFFE9FFEA),
        lightningGlow = Color(0xFF39FF7A),
        plasmaCore = Color(0xFF73FF9B),
        flashColor = Color(0xFFCFFFD9)
    )

    val VioletArc = LightningTheme(
        name = "VioletArc",
        background = Color(0xFF0C0718),
        panel = Color(0xAA1A1330),
        textPrimary = Color.White,
        textSecondary = Color(0xFFE1D7FF),
        lightningCore = Color(0xFFF7F2FF),
        lightningGlow = Color(0xFF9F78FF),
        plasmaCore = Color(0xFFC4A8FF),
        flashColor = Color(0xFFE7DDFF)
    )

    val CyanLab = LightningTheme(
        name = "CyanLab",
        background = Color(0xFF041318),
        panel = Color(0xAA0C2229),
        textPrimary = Color.White,
        textSecondary = Color(0xFFD3F8FF),
        lightningCore = Color(0xFFF0FEFF),
        lightningGlow = Color(0xFF23E5FF),
        plasmaCore = Color(0xFF7EF2FF),
        flashColor = Color(0xFFD8FDFF)
    )

    val SolarGold = LightningTheme(
        name = "SolarGold",
        background = Color(0xFF171003),
        panel = Color(0xAA2A2009),
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFEDBF),
        lightningCore = Color(0xFFFFFBEC),
        lightningGlow = Color(0xFFFFC531),
        plasmaCore = Color(0xFFFFD870),
        flashColor = Color(0xFFFFF1C8)
    )

    val ToxicMagenta = LightningTheme(
        name = "ToxicMagenta",
        background = Color(0xFF170513),
        panel = Color(0xAA2A0B22),
        textPrimary = Color.White,
        textSecondary = Color(0xFFFFD8F7),
        lightningCore = Color(0xFFFFF2FD),
        lightningGlow = Color(0xFFFF38C7),
        plasmaCore = Color(0xFFFF82DE),
        flashColor = Color(0xFFFFD8F4)
    )

    val IceWhite = LightningTheme(
        name = "IceWhite",
        background = Color(0xFF0B1217),
        panel = Color(0xAA152029),
        textPrimary = Color.White,
        textSecondary = Color(0xFFDCEAF4),
        lightningCore = Color(0xFFFFFFFF),
        lightningGlow = Color(0xFFA7D8FF),
        plasmaCore = Color(0xFFD8EEFF),
        flashColor = Color.White
    )

    val All = listOf(
        BlueStorm,
        RedAlert,
        HackerGreen,
        VioletArc,
        CyanLab,
        SolarGold,
        ToxicMagenta,
        IceWhite
    )
}

@Immutable
data class LightningConfig(
    val enabled: Boolean = true,
    val autoRandomEnabled: Boolean = true,
    val autoMinDelayMs: Long = 650,
    val autoMaxDelayMs: Long = 2200,
    val flashEnabled: Boolean = true,
    val flashMaxAlpha: Float = 0.16f,
    val touchLaunchEnabled: Boolean = true,
    val longPressPlasmaEnabled: Boolean = true,
    val shockRingEnabled: Boolean = true,
    val plasmaAnchorRingEnabled: Boolean = true,
    val plasmaAfterimageEnabled: Boolean = true
)

@Immutable
data class LightningDemoState(
    val theme: LightningTheme = LightningThemes.BlueStorm,
    val config: LightningConfig = LightningConfig()
)

internal data class LightningBolt(
    val id: Long,
    val points: List<Offset>,
    val branches: List<List<Offset>>,
    val bornAtMs: Long,
    val lifeMs: Long,
    val strokeWidth: Float,
    val glowWidth: Float,
    val alpha: Float,
    val flashStrength: Float,
    val isPlasmaBolt: Boolean
)

internal data class PlasmaState(
    val pointerId: PointerId,
    val position: Offset,
    val bornAtMs: Long
)

internal data class ShockRingState(
    val center: Offset,
    val bornAtMs: Long,
    val lifeMs: Long
)