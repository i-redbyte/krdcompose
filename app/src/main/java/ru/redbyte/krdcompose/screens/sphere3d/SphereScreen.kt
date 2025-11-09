package ru.redbyte.krdcompose.screens.sphere3d

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.others.sphere3d.Float3
import ru.redbyte.krdcompose.others.sphere3d.Sphere3D

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SphereScreen() {
    val icon = ImageBitmap.imageResource(id = R.drawable.ic_food)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("3D Sphere Demo") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Yellow)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {

            Sphere3D(
                modifier = Modifier.fillMaxSize(),
                sphereColor = Color(0xFF1E88E5),
                lightDirection = Float3(0.2f, 0.7f, 1f),
                useImage = false,
                image = icon,
                useText = true,
                text = "C++",
                textColor = Color.White,
                textSizeSp = 18f,
            )
        }
    }
}