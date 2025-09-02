package ru.redbyte.krdcompose.games.sudoku.model

data class SudokuPuzzle(
    val givens: IntArray,
    val solution: IntArray,
    val difficulty: Difficulty
) {
    fun clueCount(): Int = givens.count { it != 0 }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuPuzzle

        if (!givens.contentEquals(other.givens)) return false
        if (!solution.contentEquals(other.solution)) return false
        if (difficulty != other.difficulty) return false

        return true
    }

    override fun hashCode(): Int {
        var result = givens.contentHashCode()
        result = 31 * result + solution.contentHashCode()
        result = 31 * result + difficulty.hashCode()
        return result
    }
}