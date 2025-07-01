package ru.redbyte.krdcompose.sapper

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.redbyte.krdcompose.sapper.model.Cell
import ru.redbyte.krdcompose.sapper.model.Difficulty
import ru.redbyte.krdcompose.sapper.model.GameCommand
import ru.redbyte.krdcompose.sapper.model.GameState

internal class SapperViewModel : ViewModel() {
    var rows: Int = 8
        private set
    var cols: Int = 8
        private set

    private val boardFactory: BoardFactory = DefaultBoardFactory()
    private val timer = GameTimer(viewModelScope)

    private val _board: MutableState<List<List<Cell>>> = mutableStateOf(emptyList())
    val board: State<List<List<Cell>>> = _board

    var gameState: GameState by mutableStateOf(GameState.Idle)
        private set

    var elapsedTime by mutableIntStateOf(0)
        private set
    var flagsPlaced by mutableIntStateOf(0)
        private set
    var totalMines by mutableIntStateOf(0)
        private set
    private val commandsStack = ArrayDeque<GameCommand>()

    fun startNewGame(rows: Int, cols: Int, difficulty: Difficulty) {
        this.rows = rows
        this.cols = cols
        totalMines = calculateMineCount(rows, cols, difficulty)
        _board.value = boardFactory.createBoard(rows, cols, totalMines)
        gameState = GameState.Running
        elapsedTime = 0
        flagsPlaced = 0
        timer.start { elapsedTime++ }
    }

    fun executeCommand(command: GameCommand) {
        if (gameState != GameState.Running) return
        command.execute()
        commandsStack.add(command)
    }

    private fun calculateMineCount(rows: Int, cols: Int, difficulty: Difficulty): Int {
        val totalCells = rows * cols
        return when (difficulty) {
            Difficulty.EASY -> (totalCells * 0.1).toInt() // 10%
            Difficulty.MEDIUM -> (totalCells * 0.25).toInt() // 25%
            Difficulty.HARD -> (totalCells * 0.35).toInt() // 35%
        }.coerceAtLeast(1)
    }

    fun revealCell(cell: Cell) {
        if (cell.isRevealed || cell.isFlagged) return
        updateCell(cell.copy(isRevealed = true))
        if (cell.isMine) {
            gameState = GameState.Lost
            timer.stop()
        } else if (cell.neighborMines == 0) {
            revealAdjacentCells(cell)
        }
        if (checkWin()) {
            gameState = GameState.Won
            timer.stop()
        }
    }

    fun toggleFlag(cell: Cell) {
        if (cell.isRevealed) return
        updateCell(cell.copy(isFlagged = !cell.isFlagged))
        flagsPlaced = board.value.flatten().count { it.isFlagged }
    }

    private fun updateCell(updated: Cell) {
        _board.value = board.value.map { row ->
            row.map { if (it.row == updated.row && it.col == updated.col) updated else it }
        }
    }

    private fun revealAdjacentCells(start: Cell) {
        val queue = ArrayDeque<Cell>().apply { add(start) }
        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(start.row to start.col)
        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            directions.forEach { (dr, dc) ->
                val nr = cell.row + dr
                val nc = cell.col + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    val neighbor = board.value[nr][nc]
                    if (!neighbor.isRevealed && !neighbor.isFlagged) {
                        updateCell(neighbor.copy(isRevealed = true))
                        if (neighbor.neighborMines == 0 && visited.add(nr to nc)) {
                            queue.add(neighbor)
                        }
                    }
                }
            }
        }
    }

    private fun checkWin(): Boolean = board.value.flatten()
        .none { !it.isMine && !it.isRevealed }

    companion object {
        val directions =
            listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    }
}