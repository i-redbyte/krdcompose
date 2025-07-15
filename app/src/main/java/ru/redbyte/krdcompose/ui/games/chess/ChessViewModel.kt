package ru.redbyte.krdcompose.ui.games.chess

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }
enum class PieceColor { WHITE, BLACK }
data class Piece(val type: PieceType, val color: PieceColor)

data class GameState(
    val board: Array<Array<Piece?>>,
    val currentTurn: PieceColor,
    val selected: Pair<Int, Int>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentDeepEquals(other.board)) return false
        if (currentTurn != other.currentTurn) return false
        if (selected != other.selected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentTurn.hashCode()
        result = 31 * result + (selected?.hashCode() ?: 0)
        return result
    }
}

class ChessViewModel : ViewModel() {
    private val startBoard = Array(8) { Array<Piece?>(8) { null } }.apply {
        this[0] = arrayOf(
            Piece(PieceType.ROOK, PieceColor.WHITE),
            Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Piece(PieceType.BISHOP, PieceColor.WHITE),
            Piece(PieceType.QUEEN, PieceColor.WHITE),
            Piece(PieceType.KING, PieceColor.WHITE),
            Piece(PieceType.BISHOP, PieceColor.WHITE),
            Piece(PieceType.KNIGHT, PieceColor.WHITE),
            Piece(PieceType.ROOK, PieceColor.WHITE)
        )
        this[1] = Array(8) { Piece(PieceType.PAWN, PieceColor.WHITE) }
        this[7] = arrayOf(
            Piece(PieceType.ROOK, PieceColor.BLACK),
            Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Piece(PieceType.BISHOP, PieceColor.BLACK),
            Piece(PieceType.QUEEN, PieceColor.BLACK),
            Piece(PieceType.KING, PieceColor.BLACK),
            Piece(PieceType.BISHOP, PieceColor.BLACK),
            Piece(PieceType.KNIGHT, PieceColor.BLACK),
            Piece(PieceType.ROOK, PieceColor.BLACK)
        )
        this[6] = Array(8) { Piece(PieceType.PAWN, PieceColor.BLACK) }
    }
    private val _gameState = MutableStateFlow(
        GameState(
            board = startBoard,
            currentTurn = if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
        )
    )
    val gameState = _gameState.asStateFlow()
    fun onSquareClick(row: Int, col: Int) {
        val state = _gameState.value
        val piece = state.board[row][col]
        if (state.selected == null) {
            if (piece != null && piece.color == state.currentTurn) {
                _gameState.value = state.copy(selected = Pair(row, col))
            }
        } else {
            val (fromRow, fromCol) = state.selected
            if (isLegalMove(fromRow, fromCol, row, col, state)) {
                val newBoard = state.board.map { it.copyOf() }.toTypedArray()
                newBoard[row][col] = newBoard[fromRow][fromCol]
                newBoard[fromRow][fromCol] = null
                val movedPiece = newBoard[row][col]!!
                if (movedPiece.type == PieceType.PAWN && (row == 0 || row == 7)) {
                    newBoard[row][col] = Piece(PieceType.QUEEN, movedPiece.color)
                }
                val nextTurn =
                    if (state.currentTurn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                _gameState.value = GameState(board = newBoard, currentTurn = nextTurn)
            }
            _gameState.value = _gameState.value.copy(selected = null)
        }
    }

    private fun isLegalMove(fr: Int, fc: Int, tr: Int, tc: Int, state: GameState): Boolean {
        val piece = state.board[fr][fc] ?: return false
        val target = state.board[tr][tc]
        if (target != null && target.color == piece.color) return false
        val dr = tr - fr
        val dc = tc - fc
        return when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) 1 else -1
                val startRow = if (piece.color == PieceColor.WHITE) 1 else 6
                (dc == 0 && state.board[tr][tc] == null && (
                        dr == dir || (fr == startRow && dr == 2 * dir && state.board[fr + dir][fc] == null)
                        )) ||
                        (abs(dc) == 1 && dr == dir && state.board[tr][tc] != null)
            }

            PieceType.KNIGHT -> (abs(dr) == 1 && abs(dc) == 2) || (abs(dr) == 2 && abs(dc) == 1)
            PieceType.BISHOP -> abs(dr) == abs(dc) && isPathClear(fr, fc, tr, tc, state)
            PieceType.ROOK -> (dr == 0 || dc == 0) && isPathClear(fr, fc, tr, tc, state)
            PieceType.QUEEN -> (abs(dr) == abs(dc) || dr == 0 || dc == 0) && isPathClear(
                fr,
                fc,
                tr,
                tc,
                state
            )

            PieceType.KING -> max(abs(dr), abs(dc)) == 1
        }
    }

    private fun isPathClear(fr: Int, fc: Int, tr: Int, tc: Int, state: GameState): Boolean {
        val dRow = tr.compareTo(fr).coerceIn(-1, 1)
        val dCol = tc.compareTo(fc).coerceIn(-1, 1)
        var r = fr + dRow
        var c = fc + dCol
        while (r != tr || c != tc) {
            if (state.board[r][c] != null) return false
            r += dRow; c += dCol
        }
        return true
    }
}
