package ru.redbyte.krdcompose.screens.crack

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.others.crack.CrackRoot
import ru.redbyte.krdcompose.others.crack.rememberCrackState

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
                        listOf(
                            Color(0xFF00897B),
                            Color(0xFF43A047),
                            Color(color = 0xFF00897B)
                        )
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
                    "Жмите по экрану — появятся трещины.\n" +
                            "Повторный тап по существующей трещине — усилит её.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE6EAF5),
                    textAlign = TextAlign.Center
                )

                Button(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .align(Alignment.CenterHorizontally),
                    onClick = {

                    }
                ) {
                    Text("Красивая кнопка")
                }
            }
        }
    }
}