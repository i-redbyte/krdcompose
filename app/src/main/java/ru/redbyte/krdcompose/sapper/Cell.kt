package ru.redbyte.krdcompose.sapper

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal data class Cell(
    val row: Int,
    val col: Int,
    var isMine: Boolean = false,
    var neighborMines: Int = 0
) {
    var isRevealed by mutableStateOf(false)
    var isFlagged  by mutableStateOf(false)
}