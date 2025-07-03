package ru.redbyte.krdcompose.screens.pascalTriangle

import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import ru.redbyte.krdcompose.ui.components.PascalTriangle

@Composable
internal fun PascalTriangleScreen() {
    val context = LocalContext.current
    var rows by remember { mutableIntStateOf(6) }
    val rowColors = listOf(
        Color(0xFF009688),
        Color(0xFFB71C1C),
        Color(0xFF1B5E20),
        Color(0xFF0D47A1),
        Color(0xFFF57F17),
        Color(0xFF4A148C)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Rows: $rows", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = rows.toFloat(),
            onValueChange = { rows = it.toInt().coerceAtLeast(1) },
            valueRange = 1f..12f
        )
        PascalTriangle(
            numRows = rows,
            rowColors = rowColors,
            cellText = { row, col, value -> "\uD83C\uDF7A" },
            onCellClick = { row, col, value ->
                Toast
                    .makeText(
                        context,
                        "Clicked position: ${row + 1}:${col + 1}; value = $value",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}