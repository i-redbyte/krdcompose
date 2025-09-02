package ru.redbyte.krdcompose.games.sudoku.model

sealed interface CellContent {
    data object Empty : CellContent
    data class Value(val v: Int) : CellContent
    data class Notes(val set: Set<Int>) : CellContent
}