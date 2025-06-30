package ru.redbyte.krdcompose.sapper

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


internal class SapperViewModel : ViewModel() {
    var rows: Int = 8
    var cols: Int = 8
    var totalMines: Int = 10

    private val _board: MutableState<List<List<Cell>>> = mutableStateOf(emptyList())
    val board: State<List<List<Cell>>> = _board

    var gameRunning by mutableStateOf(false)
    var gameOver by mutableStateOf(false)
    var gameWon by mutableStateOf(false)

    var elapsedTime by mutableIntStateOf(0)
        private set
    var flagsPlaced by mutableIntStateOf(0)

    private var timerJob: Job? = null

    init {
        startNewGame(rows, cols, Difficulty.EASY)
    }

    fun startNewGame(rows: Int, cols: Int, difficulty: Difficulty) {
        this.rows = rows
        this.cols = cols
        totalMines = calculateMineCount(rows, cols, difficulty)
        _board.value = generateBoard(rows, cols, totalMines)
        gameRunning = true
        gameOver = false
        gameWon = false
        elapsedTime = 0
        flagsPlaced = 0
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            try {
                while (true) {
                    delay(1_000)
                    if (!gameRunning) break
                    elapsedTime += 1
                }
            } finally {
                /* no-op */
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun calculateMineCount(rows: Int, cols: Int, difficulty: Difficulty): Int {
        val totalCells = rows * cols
        return when (difficulty) {
            Difficulty.EASY -> (totalCells * 0.1).toInt()    // ~10% mines
            Difficulty.MEDIUM -> (totalCells * 0.15).toInt()  // ~15% mines
            Difficulty.HARD -> (totalCells * 0.2).toInt()    // ~20% mines
        }.coerceAtLeast(1)
    }

    private fun generateBoard(rows: Int, cols: Int, mines: Int): List<List<Cell>> {
        val newBoard = List(rows) { r -> List(cols) { c -> Cell(row = r, col = c) } }
        placeMines(newBoard, mines)
        calculateNeighborCounts(newBoard)
        return newBoard
    }

    private fun placeMines(board: List<List<Cell>>, mines: Int) {
        var placed = 0
        while (placed < mines) {
            val r = (0 until rows).random()
            val c = (0 until cols).random()
            val cell = board[r][c]
            if (!cell.isMine) {
                cell.isMine = true
                placed++
            }
        }
    }

    private fun calculateNeighborCounts(board: List<List<Cell>>) {
        val directions =
            listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (cell.isMine) {
                    cell.neighborMines = -1
                } else {
                    cell.neighborMines = directions.count { (dr, dc) ->
                        val nr = r + dr
                        val nc = c + dc
                        nr in 0 until rows && nc in 0 until cols && board[nr][nc].isMine
                    }
                }
            }
        }
    }

    fun revealCell(cell: Cell) {
        if (gameOver || gameWon || cell.isRevealed) return
        cell.isRevealed = true
        if (cell.isMine) {
            gameOver = true
            gameRunning = false
            stopTimer()
        } else {
            if (cell.neighborMines == 0) {
                revealAdjacentCells(cell)
            }
            if (checkWin()) {
                gameWon = true
                gameRunning = false
                stopTimer()
            }
        }
    }

    private fun revealAdjacentCells(start: Cell) {
        val queue: ArrayDeque<Cell> = ArrayDeque()
        queue.add(start)
        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(start.row to start.col)
        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            val directions =
                listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
            for ((dr, dc) in directions) {
                val nr = cell.row + dr
                val nc = cell.col + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    val neighbor = _board.value[nr][nc]
                    if (!neighbor.isRevealed && !neighbor.isFlagged) {
                        neighbor.isRevealed = true
                        if (neighbor.neighborMines == 0 && (nr to nc) !in visited) {
                            queue.add(neighbor)
                            visited.add(nr to nc)
                        }
                    }
                }
            }
        }
    }

    private fun checkWin(): Boolean {
        val allCells = _board.value.flatten()
        val unrevealed = allCells.count { !it.isRevealed && !it.isMine }
        return unrevealed == 0 && !gameOver
    }

    fun toggleFlag(cell: Cell) {
        if (gameOver || gameWon || cell.isRevealed) return
        cell.isFlagged = !cell.isFlagged
        flagsPlaced = _board.value.flatten().count { it.isFlagged }
    }
}
