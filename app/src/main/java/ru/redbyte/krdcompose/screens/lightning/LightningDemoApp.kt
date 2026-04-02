@file:Suppress("FunctionName")

package ru.redbyte.krdcompose.screens.lightning

import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import ru.redbyte.krdcompose.ui.components.lightning.LightningConfig
import ru.redbyte.krdcompose.ui.components.lightning.LightningDemoState
import ru.redbyte.krdcompose.ui.components.lightning.LightningThemes

private enum class DemoRoute {
    Settings,
    Demo
}

@Composable
fun LightningDemoApp() {
    var route by remember { mutableStateOf(DemoRoute.Settings) }
    var state by remember {
        mutableStateOf(
            LightningDemoState(
                theme = LightningThemes.BlueStorm,
                config = LightningConfig()
            )
        )
    }

    when (route) {
        DemoRoute.Settings -> {
            LightningSettingsScreen(
                initialState = state,
                onShowDemo = {
                    state = it
                    route = DemoRoute.Demo
                }
            )
        }

        DemoRoute.Demo -> {
            LightningDemoScreen(
                state = state,
                onBack = { route = DemoRoute.Settings }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewLightningDemoApp() {
    MaterialTheme {
        LightningDemoApp()
    }
}