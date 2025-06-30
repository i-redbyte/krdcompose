package ru.redbyte.krdcompose.sapper

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun DifficultyDropdown(
    selected: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.difficultyName.lowercase().replaceFirstChar(Char::titlecase))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Difficulty.entries.forEach { diff ->
                DropdownMenuItem(
                    text = { Text(diff.difficultyName.lowercase().replaceFirstChar(Char::titlecase)) },
                    onClick = {
                        onDifficultySelected(diff)
                        expanded = false
                    }
                )
            }
        }
    }
}