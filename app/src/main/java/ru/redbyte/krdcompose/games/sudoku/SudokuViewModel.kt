package ru.redbyte.krdcompose.games.sudoku

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.redbyte.krdcompose.games.sudoku.model.Difficulty
import ru.redbyte.krdcompose.games.sudoku.model.GameStatus
import ru.redbyte.krdcompose.games.sudoku.model.ScreenState
import ru.redbyte.krdcompose.games.sudoku.model.SudokuPuzzle

class SudokuViewModel : ViewModel() {
    private val generator = SudokuGenerator()

    var currentDifficulty: Difficulty = Difficulty.EASY
        private set

    private var currentPuzzle: SudokuPuzzle? = null

    private val _screenState = mutableStateOf(ScreenState.Menu)
    val screenState: State<ScreenState> = _screenState

    private val _generating = mutableStateOf(false)
    val generating: State<Boolean> = _generating

    private val _isTimedGame = mutableStateOf(false)
    val isTimedGame: State<Boolean> = _isTimedGame

    private val _timeRemaining = mutableStateOf(0)
    val timeRemaining: State<Int> = _timeRemaining

    private val _board = mutableStateListOf<Int>()
    val board: SnapshotStateList<Int> = _board

    // Важно: Set<Int>, каждое изменение — новое множество (рекомпозиция мгновенно)
    private val _notes = mutableStateListOf<Set<Int>>()
    val notesState: State<List<Set<Int>>> = derivedStateOf { _notes }

    private val _isCellEditable = mutableStateListOf<Boolean>()
    val isCellEditableState: State<List<Boolean>> = derivedStateOf { _isCellEditable.toList() }

    private val _selectedCell = mutableStateOf(-1)
    val selectedCell: State<Int> = _selectedCell

    private val _noteMode = mutableStateOf(false)
    val noteMode: State<Boolean> = _noteMode

    private val _gameStatus = mutableStateOf(GameStatus.Playing)
    val gameStatus: State<GameStatus> = _gameStatus

    private var timerJob: Job? = null

    init {
        repeat(81) {
            _board.add(0)
            _notes.add(emptySet())
            _isCellEditable.add(true)
        }
    }

    fun startGame(difficulty: Difficulty, timed: Boolean) {
        timerJob?.cancel()
        currentDifficulty = difficulty
        currentPuzzle = null
        _gameStatus.value = GameStatus.Playing
        _screenState.value = ScreenState.Game
        _generating.value = true
        _isTimedGame.value = timed
        _selectedCell.value = -1
        _noteMode.value = false

        viewModelScope.launch {
            val puzzle = generator.generate(difficulty)
            currentPuzzle = puzzle
            for (i in 0 until 81) {
                _board[i] = puzzle.givens[i]
                _notes[i] = emptySet()
                _isCellEditable[i] = puzzle.givens[i] == 0
            }
            if (timed) {
                _timeRemaining.value = when (difficulty) {
                    Difficulty.EASY -> 600
                    Difficulty.NORMAL -> 900
                    Difficulty.HARD -> 1200
                }
                startTimer()
            } else {
                _timeRemaining.value = 0
            }
            _generating.value = false
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0 && _gameStatus.value == GameStatus.Playing) {
                delay(1000)
                _timeRemaining.value = (_timeRemaining.value - 1).coerceAtLeast(0)
                if (_timeRemaining.value == 0 && _gameStatus.value == GameStatus.Playing) {
                    _gameStatus.value = GameStatus.Lost
                    break
                }
            }
        }
    }

    fun selectCell(index: Int) {
        if (_gameStatus.value != GameStatus.Playing) return
        if (!_isCellEditable[index]) return
        _selectedCell.value = if (_selectedCell.value == index) -1 else index
    }

    fun inputNumber(number: Int) {
        if (_gameStatus.value != GameStatus.Playing) return
        val idx = _selectedCell.value
        if (idx == -1) return
        if (!_isCellEditable[idx]) return

        if (_noteMode.value) {
            if (_board[idx] != 0) return
            val cur = _notes[idx]
            _notes[idx] = if (number in cur) cur - number else cur + number
        } else {
            if (_board[idx] == number) return
            _board[idx] = number
            _notes[idx] = emptySet()
            clearNumberFromPeersNotes(idx, number)
            checkWin()
        }
    }

    fun erase() {
        if (_gameStatus.value != GameStatus.Playing) return
        val idx = _selectedCell.value
        if (idx == -1) return
        if (!_isCellEditable[idx]) return
        _board[idx] = 0
        _notes[idx] = emptySet()
    }

    fun useHint() {
        if (_gameStatus.value != GameStatus.Playing) return
        val targetIndex = if (_selectedCell.value != -1 && _board[_selectedCell.value] == 0) {
            _selectedCell.value
        } else {
            _board.indexOfFirst { it == 0 && _isCellEditable[it] }
        }
        if (targetIndex == -1) return
        currentPuzzle?.let { puzzle ->
            val correctNum = puzzle.solution[targetIndex]
            _board[targetIndex] = correctNum
            _notes[targetIndex] = emptySet()
            _isCellEditable[targetIndex] = false
            clearNumberFromPeersNotes(targetIndex, correctNum)
            checkWin()
        }
    }

    private fun checkWin() {
        if (_board.any { it == 0 }) return
        val solved = currentPuzzle?.let { p ->
            (0 until 81).all { i -> _board[i] == p.solution[i] }
        } == true
        if (solved) {
            _gameStatus.value = GameStatus.Won
            timerJob?.cancel()
        }
    }

    fun toggleNoteMode() {
        _noteMode.value = !_noteMode.value
    }

    fun exitToMenu() {
        timerJob?.cancel()
        _screenState.value = ScreenState.Menu
    }

    private fun clearNumberFromPeersNotes(idx: Int, number: Int) {
        val r = idx / 9
        val c = idx % 9
        val br = (r / 3) * 3
        val bc = (c / 3) * 3

        for (cc in 0 until 9) {
            val i = r * 9 + cc
            val s = _notes[i]
            if (number in s) _notes[i] = s - number
        }
        for (rr in 0 until 9) {
            val i = rr * 9 + c
            val s = _notes[i]
            if (number in s) _notes[i] = s - number
        }
        for (dr in 0 until 3) for (dc in 0 until 3) {
            val i = (br + dr) * 9 + (bc + dc)
            val s = _notes[i]
            if (number in s) _notes[i] = s - number
        }
    }
}
