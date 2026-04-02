package ru.redbyte.krdcompose.screens.lightning

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.lightning.LightningBackground
import ru.redbyte.krdcompose.ui.components.lightning.LightningDemoState

@Composable
fun LightningDemoScreen(
    state: LightningDemoState,
    onBack: () -> Unit
) {
    LightningBackground(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
        config = state.config,
        theme = state.theme
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Назад к настройкам")
            }
        }
    }
}