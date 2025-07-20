package ru.redbyte.krdcompose.games.sapper

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.redbyte.krdcompose.games.sapper.model.Cell

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun SapperGameBoard(
    board: List<List<Cell>>,
    onCellClick: (Cell) -> Unit,
    onCellLongClick: (Cell) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (board.isEmpty()) return@BoxWithConstraints

        val columns  = board.first().size
        val cellSize = maxWidth / columns

        Column {
            board.forEach { row ->
                Row {
                    row.forEach { cell ->
                        SapperCell(
                            cell        = cell,
                            size        = cellSize,
                            onClick     = { onCellClick(cell) },
                            onLongClick = { onCellLongClick(cell) }
                        )
                    }
                }
            }
        }
    }
}
