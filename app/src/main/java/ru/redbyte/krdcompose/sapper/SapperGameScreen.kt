package ru.redbyte.krdcompose.sapper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.redbyte.krdcompose.sapper.model.Difficulty
import ru.redbyte.krdcompose.sapper.model.FlagCellCommand
import ru.redbyte.krdcompose.sapper.model.GameState
import ru.redbyte.krdcompose.sapper.model.RevealCellCommand

@Composable
internal fun SapperGameScreen(viewModel: SapperViewModel = viewModel()) {
    val boardState by viewModel.board
    val elapsedTime = viewModel.elapsedTime
    val minesTotal = viewModel.totalMines
    val flagsPlaced = viewModel.flagsPlaced
    val gameState = viewModel.gameState

    var chosenDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var widthInput by remember { mutableStateOf(viewModel.cols.toString()) }
    var heightInput by remember { mutableStateOf(viewModel.rows.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Время: ${elapsedTime}s", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Мины: ${minesTotal - flagsPlaced}",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(text = "Сложность:")
            DifficultyDropdown(
                selected = chosenDifficulty,
                onDifficultySelected = { diff ->
                    chosenDifficulty = diff
                    val w = widthInput.toIntOrNull() ?: viewModel.cols
                    val h = heightInput.toIntOrNull() ?: viewModel.rows
                    viewModel.startNewGame(h, w, diff)
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ширина:")
                TextField(
                    value = widthInput,
                    onValueChange = { widthInput = it },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Text(text = "Длина:")
                TextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Button(onClick = {
                    val w = widthInput.toIntOrNull() ?: viewModel.cols
                    val h = heightInput.toIntOrNull() ?: viewModel.rows
                    viewModel.startNewGame(h, w, chosenDifficulty)
                }) {
                    Text("Новая игра")
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            SapperGameBoard(
                board = boardState,
                onCellClick = { cell ->
                    viewModel
                        .executeCommand(
                            RevealCellCommand(viewModel, cell)
                        )
                },
                onCellLongClick = { cell ->
                    viewModel
                        .executeCommand(
                            FlagCellCommand(viewModel, cell)
                        )
                }
            )
        }

        if (gameState is GameState.Won || gameState is GameState.Lost) {
            val statusText = if (gameState is GameState.Won) "Вы победили!" else "Вы проиграли!"
            val statusColor =
                if (gameState is GameState.Won) Color(0xFF0F832F)
                else Color(0xFFBB0621)
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineMedium,
                color = statusColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}
