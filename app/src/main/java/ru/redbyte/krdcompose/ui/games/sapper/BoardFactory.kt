package ru.redbyte.krdcompose.ui.games.sapper

import ru.redbyte.krdcompose.ui.games.sapper.model.Cell

internal interface BoardFactory {
    fun createBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>>
}
