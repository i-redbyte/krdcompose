package ru.redbyte.krdcompose.screens.sphere3d

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.others.sphere3d.Float3
import ru.redbyte.krdcompose.others.sphere3d.Sphere3D

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SphereScreen() {
    val icon = ImageBitmap.imageResource(id = R.drawable.ic_snake_tail)

    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }
    var hue by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val sphereColor = Color.hsv(
        hue = hue,
        saturation = 0.9f,
        value = 1f
    )

    LaunchedEffect(isAnimating) {
        if (!isAnimating) return@LaunchedEffect

        val frameMs = 16L
        val rotSpeedX = 0.6f
        val rotSpeedY = 1.0f
        val hueSpeed = 60f
        val twoPi = (2.0 * kotlin.math.PI).toFloat()

        while (true) {
            val dt = frameMs / 1000f

            rotationX = (rotationX + rotSpeedX * dt) % twoPi
            rotationY = (rotationY + rotSpeedY * dt) % twoPi
            hue = (hue + hueSpeed * dt) % 360f

            kotlinx.coroutines.delay(frameMs)

            if (!isAnimating) break
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("3D Sphere Demo") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Sphere3D(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                sphereColor = sphereColor,
                lightDirection = Float3(0.2f, 0.7f, 1f),
                useImage = false,
                image = icon,
                useText = true,
                text = "Kotlin",
                textColor = Color.White,
                textSizeSp = 18f,
                rotationX = rotationX,
                rotationY = rotationY,
                onRotateDelta = { dx, dy ->
                    if (!isAnimating) {
                        val sensitivity = 0.01f
                        rotationY += dx * sensitivity
                        rotationX += dy * sensitivity
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { isAnimating = !isAnimating }
            ) {
                Text(if (isAnimating) "Пауза" else "Вращать")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
