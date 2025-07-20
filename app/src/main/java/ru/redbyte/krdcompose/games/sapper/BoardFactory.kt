package ru.redbyte.krdcompose.games.sapper

import ru.redbyte.krdcompose.games.sapper.model.Cell

internal interface BoardFactory {
    fun createBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>>
}
