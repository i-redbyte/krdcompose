package ru.redbyte.krdcompose.screens.sudoku

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.redbyte.krdcompose.games.sudoku.SudokuViewModel
import ru.redbyte.krdcompose.games.sudoku.model.ScreenState

@Composable
fun SudokuApp(viewModel: SudokuViewModel = viewModel()) {
    val screenState by viewModel.screenState
    when (screenState) {
        ScreenState.Menu -> DifficultyScreen(
            onStartGame = { diff, timed ->
                viewModel.startGame(diff, timed)
            }
        )

        ScreenState.Game -> SudokuGameScreen(viewModel)
    }
}
