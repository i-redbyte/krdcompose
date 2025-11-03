package ru.redbyte.krdcompose.others.crack

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    val crackState = rememberCrackState()

    CrackRoot(state = crackState, distortion = true) {
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
}