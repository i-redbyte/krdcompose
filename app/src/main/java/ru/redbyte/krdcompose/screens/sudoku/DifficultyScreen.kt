package ru.redbyte.krdcompose.screens.sudoku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.games.sudoku.model.Difficulty

@Composable
fun DifficultyScreen(onStartGame: (Difficulty, Boolean) -> Unit) {
    var selectedDifficulty by rememberSaveable { mutableStateOf(Difficulty.EASY) }
    var timedMode by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            elevation = 2.dp
        ) {
            Column {
                Difficulty.entries.forEachIndexed { index, difficulty ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { selectedDifficulty = difficulty }
                            .padding(horizontal = 16.dp)
                    ) {
                        RadioButton(
                            selected = (difficulty == selectedDifficulty),
                            onClick = { selectedDifficulty = difficulty }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = difficulty.name,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index != Difficulty.entries.lastIndex) {
                        Divider()
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Timed Mode",
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = timedMode,
                onCheckedChange = { timedMode = it }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onStartGame(selectedDifficulty, timedMode) },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Text(text = "Start Game")
        }
    }
}
