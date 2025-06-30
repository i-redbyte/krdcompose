package ru.redbyte.krdcompose.sapper

import ru.redbyte.krdcompose.sapper.model.Cell

internal interface BoardFactory {
    fun createBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>>
}
