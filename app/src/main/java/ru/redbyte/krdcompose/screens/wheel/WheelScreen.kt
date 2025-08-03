package ru.redbyte.krdcompose.screens.wheel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.games.fortuneWheel.FortuneWheel
import ru.redbyte.krdcompose.games.fortuneWheel.WheelItem
import ru.redbyte.krdcompose.games.fortuneWheel.easingMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen() {
    val items = remember {
        listOf(
            WheelItem("OCamel"),
            WheelItem("C"),
            WheelItem("Kotlin"),
            WheelItem("Python"),
            WheelItem("Java"),
            WheelItem("C++"),
            WheelItem("Haskell"),
            WheelItem("PHP"),
            WheelItem("C#"),
            WheelItem("Perl")
        )
    }

    var selected by remember { mutableStateOf<WheelItem?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var easingName by remember { mutableStateOf(easingMap.keys.first()) }
    val easing = easingMap[easingName]!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = easingName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Easing") },
                modifier = Modifier.menuAnchor(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                easingMap.keys.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            easingName = name
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        FortuneWheel(
            items = items,
            modifier = Modifier.fillMaxWidth(0.9f),
            evenSectorColor = Color(0xFF071D9A),
            oddSectorColor = Color(0xFF000000),
            evenTextColor = Color(0xFFECF6EC),
            oddTextColor = Color(0xFFFF9800),
            easing = easing,
            onItemSelected = { selected = it }
        )
        Spacer(Modifier.height(24.dp))
        selected?.let {
            Text("Выпало: ${it.text}", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
