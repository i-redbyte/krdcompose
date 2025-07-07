package ru.redbyte.krdcompose.screens.numberSystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.BitColorScheme
import ru.redbyte.krdcompose.ui.components.NumberSystemConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSystemScreen() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Decimal ↔ Hex Converter") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                NumberSystemConverter(
                    bitColorScheme = BitColorScheme.GROUP_8,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}