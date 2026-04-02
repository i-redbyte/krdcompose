package ru.redbyte.krdcompose.screens.lightning

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.lightning.LightningConfig
import ru.redbyte.krdcompose.ui.components.lightning.LightningDemoState
import ru.redbyte.krdcompose.ui.components.lightning.LightningThemes
import ru.redbyte.krdcompose.ui.components.lightning.SettingSwitch

@Composable
fun LightningSettingsScreen(
    initialState: LightningDemoState,
    onShowDemo: (LightningDemoState) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(initialState.theme) }

    var autoRandom by remember { mutableStateOf(initialState.config.autoRandomEnabled) }
    var flash by remember { mutableStateOf(initialState.config.flashEnabled) }
    var touchLaunch by remember { mutableStateOf(initialState.config.touchLaunchEnabled) }
    var plasma by remember { mutableStateOf(initialState.config.longPressPlasmaEnabled) }
    var shockRing by remember { mutableStateOf(initialState.config.shockRingEnabled) }
    var anchorRing by remember { mutableStateOf(initialState.config.plasmaAnchorRingEnabled) }
    var afterimage by remember { mutableStateOf(initialState.config.plasmaAfterimageEnabled) }

    val state = remember(
        selectedTheme,
        autoRandom,
        flash,
        touchLaunch,
        plasma,
        shockRing,
        anchorRing,
        afterimage
    ) {
        LightningDemoState(
            theme = selectedTheme,
            config = LightningConfig(
                enabled = true,
                autoRandomEnabled = autoRandom,
                flashEnabled = flash,
                touchLaunchEnabled = touchLaunch,
                longPressPlasmaEnabled = plasma,
                shockRingEnabled = shockRing,
                plasmaAnchorRingEnabled = anchorRing,
                plasmaAfterimageEnabled = afterimage
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(selectedTheme.background)
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = selectedTheme.panel),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Настройки Lightning Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = selectedTheme.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Тема",
                    color = selectedTheme.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LightningThemes.All.forEach { theme ->
                        FilterChip(
                            selected = selectedTheme.name == theme.name,
                            onClick = { selectedTheme = theme },
                            label = { Text(theme.name) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SettingSwitch("Авто-случайные молнии", autoRandom, selectedTheme) {
                    autoRandom = it
                }
                SettingSwitch("Вспышка", flash, selectedTheme) { flash = it }
                SettingSwitch("Молния от касания", touchLaunch, selectedTheme) { touchLaunch = it }
                SettingSwitch("Долгое зажатие / plasma", plasma, selectedTheme) { plasma = it }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = selectedTheme.textSecondary.copy(alpha = 0.25f))
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Plasma-эффекты",
                    color = selectedTheme.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                SettingSwitch("Shock ring", shockRing, selectedTheme) { shockRing = it }
                SettingSwitch("Anchor ring", anchorRing, selectedTheme) { anchorRing = it }
                SettingSwitch("Afterimage", afterimage, selectedTheme) { afterimage = it }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onShowDemo(state) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Посмотреть демо")
                }
            }
        }
    }
}