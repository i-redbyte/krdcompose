package ru.redbyte.krdcompose.ui.games.sapper

import ru.redbyte.krdcompose.ui.games.sapper.model.Cell

internal class DefaultBoardFactory : BoardFactory {
    override fun createBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>> {
        val cells = List(rows * cols) { i -> Cell(row = i / cols, col = i % cols) }.shuffled()
        val board = cells.mapIndexed { idx, cell ->
            if (idx < mines) cell.copy(isMine = true) else cell
        }.sortedBy { it.row * cols + it.col }.chunked(cols)

        return board.map { row ->
            row.map { cell ->
                if (cell.isMine) cell.copy(neighborMines = -1)
                else cell.copy(neighborMines = SapperViewModel.directions.count { (dr, dc) ->
                    val nr = cell.row + dr
                    val nc = cell.col + dc
                    nr in 0 until rows && nc in 0 until cols && board[nr][nc].isMine
                })
            }
        }
    }
}