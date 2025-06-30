package ru.redbyte.krdcompose.sapper

internal interface BoardFactory {
    fun createBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>>
}
