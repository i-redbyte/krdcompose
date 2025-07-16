package ru.redbyte.krdcompose.ui.games.chess

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.redbyte.krdcompose.ui.games.chess.model.GameState
import ru.redbyte.krdcompose.ui.games.chess.model.GameStatus
import ru.redbyte.krdcompose.ui.games.chess.model.Move
import ru.redbyte.krdcompose.ui.games.chess.model.Piece
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor.BLACK
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor.WHITE
import ru.redbyte.krdcompose.ui.games.chess.model.PieceType
import ru.redbyte.krdcompose.ui.games.chess.model.PieceType.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class ChessViewModel() : ViewModel() {

    private fun deepCopyBoard(src: Array<Array<Piece?>>) =
        Array(8) { r -> Array(8) { c -> src[r][c] } }

    private fun initialBoard(): Array<Array<Piece?>> {
        val b = Array(8) { arrayOfNulls<Piece>(8) }
        for (c in 0..7) {
            b[1][c] = Piece(PAWN, BLACK)
            b[6][c] = Piece(PAWN, WHITE)
        }
        b[0][0] = Piece(ROOK, BLACK)
        b[0][7] = Piece(ROOK, BLACK)
        b[0][1] = Piece(KNIGHT, BLACK)
        b[0][6] = Piece(KNIGHT, BLACK)
        b[0][2] = Piece(BISHOP, BLACK)
        b[0][5] = Piece(BISHOP, BLACK)
        b[0][3] = Piece(QUEEN, BLACK)
        b[0][4] = Piece(KING, BLACK)
        b[7][0] = Piece(ROOK, WHITE)
        b[7][7] = Piece(ROOK, WHITE)
        b[7][1] = Piece(KNIGHT, WHITE)
        b[7][6] = Piece(KNIGHT, WHITE)
        b[7][2] = Piece(BISHOP, WHITE)
        b[7][5] = Piece(BISHOP, WHITE)
        b[7][3] = Piece(QUEEN, WHITE)
        b[7][4] = Piece(KING, WHITE)
        return b
    }

    private val _state = MutableStateFlow(
        GameState(
            board = initialBoard(),
            currentTurn = if (Random.nextBoolean()) WHITE else BLACK,
            whiteCastleKingside = true,
            whiteCastleQueenside = true,
            blackCastleKingside = true,
            blackCastleQueenside = true,
            enPassantTarget = null,
            selected = null,
            moves = emptyList(),
            status = GameStatus.RUNNING
        )
    )
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val history = mutableListOf<GameState>()

    fun onSquareClick(row: Int, col: Int) {
        val st = _state.value
        val sel = st.selected
        val piece = st.board[row][col]
        if (sel == null) {
            if (piece != null && piece.color == st.currentTurn) {
                _state.value = st.copy(selected = Pair(row, col))
            }
        } else {
            val selPiece = st.board[sel.first][sel.second]
            if (
                selPiece?.type == KING &&
                piece?.type == ROOK &&
                piece.color == selPiece.color
            ) {
                val toCol = if (col == 7) 6 else 2
                val m = legalMovesForPiece(sel.first, sel.second, st)
                    .firstOrNull { it.isCastling && it.toCol == toCol }
                if (m != null) {
                    makeMove(m)
                    return
                }
            }
            val candidate = legalMovesForPiece(sel.first, sel.second, st)
                .firstOrNull { it.toRow == row && it.toCol == col }
            if (candidate != null) makeMove(candidate)
            else _state.value = st.copy(selected = null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun undo() {
        if (history.isNotEmpty()) _state.value = history.removeLast()
    }

    private fun makeMove(move: Move) {
        val st = _state.value
        history += st.copy(board = deepCopyBoard(st.board))
        val board = deepCopyBoard(st.board)
        val piece = board[move.fromRow][move.fromCol]!!
        board[move.fromRow][move.fromCol] = null
        var wCK = st.whiteCastleKingside
        var wCQ = st.whiteCastleQueenside
        var bCK = st.blackCastleKingside
        var bCQ = st.blackCastleQueenside
        var enTarget: Pair<Int, Int>? = null
        if (move.isCastling) {
            board[move.toRow][move.toCol] = piece
            if (piece.color == WHITE) {
                if (move.toCol == 6) {
                    val rook = board[7][7]!!
                    board[7][7] = null
                    board[7][5] = rook
                } else {
                    val rook = board[7][0]!!
                    board[7][0] = null
                    board[7][3] = rook
                }
                wCK = false; wCQ = false
            } else {
                if (move.toCol == 6) {
                    val rook = board[0][7]!!
                    board[0][7] = null
                    board[0][5] = rook
                } else {
                    val rook = board[0][0]!!
                    board[0][0] = null
                    board[0][3] = rook
                }
                bCK = false; bCQ = false
            }
        } else if (move.isEnPassant) {
            board[move.toRow][move.toCol] = piece
            val dir = if (piece.color == WHITE) 1 else -1
            board[move.toRow + dir][move.toCol] = null
        } else {
            board[move.toRow][move.toCol] =
                if (move.promotion != null) Piece(move.promotion, piece.color) else piece
        }
        when (piece.type) {
            KING -> if (piece.color == WHITE) {
                wCK = false; wCQ = false
            } else {
                bCK = false; bCQ = false
            }

            ROOK -> {
                if (piece.color == WHITE) {
                    if (move.fromRow == 7 && move.fromCol == 0) wCQ = false
                    if (move.fromRow == 7 && move.fromCol == 7) wCK = false
                } else {
                    if (move.fromRow == 0 && move.fromCol == 0) bCQ = false
                    if (move.fromRow == 0 && move.fromCol == 7) bCK = false
                }
            }

            else -> {}
        }
        if (piece.type == PAWN && abs(move.toRow - move.fromRow) == 2) {
            val dir = if (piece.color == WHITE) -1 else 1
            enTarget = Pair(move.fromRow + dir, move.fromCol)
        }
        val nextTurn = opposite(st.currentTurn)
        val kingSq = findKing(nextTurn, board)
        val kingInCheck = attacked(kingSq.first, kingSq.second, st.currentTurn, board)
        val legal = mutableListOf<Move>()
        for (r in 0..7) for (c in 0..7)
            if (board[r][c]?.color == nextTurn)
                legal += legalMovesForPiece(r, c, st.copy(board = board, currentTurn = nextTurn))

        val newStatus = when {
            legal.isNotEmpty() && kingInCheck && nextTurn == WHITE -> GameStatus.CHECK_WHITE
            legal.isNotEmpty() && kingInCheck -> GameStatus.CHECK_BLACK
            legal.isEmpty() && kingInCheck && nextTurn == WHITE -> GameStatus.MATE_BLACK_WINS
            legal.isEmpty() && kingInCheck -> GameStatus.MATE_WHITE_WINS
            legal.isEmpty() -> GameStatus.STALEMATE
            else -> GameStatus.RUNNING
        }
        _state.value = GameState(
            board = board,
            currentTurn = nextTurn,
            whiteCastleKingside = wCK,
            whiteCastleQueenside = wCQ,
            blackCastleKingside = bCK,
            blackCastleQueenside = bCQ,
            enPassantTarget = enTarget,
            selected = null,
            moves = st.moves + algebraic(move),
            status = newStatus
        )
    }

    private fun algebraic(m: Move): String {
        val files = "abcdefgh"
        val from = "${files[m.fromCol]}${8 - m.fromRow}"
        val to = "${files[m.toCol]}${8 - m.toRow}"
        val promo = m.promotion?.let {
            when (it) {
                QUEEN -> "q"
                ROOK -> "r"
                BISHOP -> "b"
                KNIGHT -> "n"
                else -> ""
            }
        } ?: ""
        return from + to + promo
    }

    private fun legalMovesForPiece(r: Int, c: Int, st: GameState): List<Move> {
        val piece = st.board[r][c] ?: return emptyList()
        val raw = pseudoMoves(r, c, st)
        val res = mutableListOf<Move>()
        for (m in raw) {
            val temp = deepCopyBoard(st.board)
            temp[m.toRow][m.toCol] = temp[r][c]
            temp[r][c] = null
            if (m.isCastling) {
                if (piece.color == WHITE) {
                    if (m.toCol == 6) {
                        temp[7][5] = temp[7][7]; temp[7][7] = null
                    } else {
                        temp[7][3] = temp[7][0]; temp[7][0] = null
                    }
                } else {
                    if (m.toCol == 6) {
                        temp[0][5] = temp[0][7]; temp[0][7] = null
                    } else {
                        temp[0][3] = temp[0][0]; temp[0][0] = null
                    }
                }
            }
            if (m.isEnPassant) {
                val dir = if (piece.color == WHITE) 1 else -1
                temp[m.toRow + dir][m.toCol] = null
            }
            val kp = findKing(piece.color, temp)
            if (!attacked(kp.first, kp.second, opposite(piece.color), temp)) res += m
        }
        return res
    }

    private fun findKing(color: PieceColor, board: Array<Array<Piece?>>): Pair<Int, Int> {
        for (r in 0..7) for (c in 0..7)
            if (board[r][c]?.type == KING && board[r][c]?.color == color) return Pair(
                r,
                c
            )
        return Pair(-1, -1)
    }

    private fun pseudoMoves(r: Int, c: Int, st: GameState): List<Move> {
        val p = st.board[r][c] ?: return emptyList()
        val list = mutableListOf<Move>()
        when (p.type) {
            PAWN -> {
                val dir = if (p.color == WHITE) -1 else 1
                val start = if (p.color == WHITE) 6 else 1
                val one = r + dir
                if (one in 0..7 && st.board[one][c] == null) {
                    if (one == 0 || one == 7) {
                        list += Move(r, c, one, c, QUEEN)
                        list += Move(r, c, one, c, ROOK)
                        list += Move(r, c, one, c, BISHOP)
                        list += Move(r, c, one, c, KNIGHT)
                    } else list += Move(r, c, one, c)
                    if (r == start && st.board[r + 2 * dir][c] == null)
                        list += Move(r, c, r + 2 * dir, c)
                }
                for (dc in listOf(-1, 1)) {
                    val nc = c + dc
                    if (nc !in 0..7) continue
                    val target = st.board[one][nc]
                    if (target != null && target.color != p.color)
                        if (one == 0 || one == 7) {
                            list += Move(r, c, one, nc, QUEEN)
                            list += Move(r, c, one, nc, ROOK)
                            list += Move(r, c, one, nc, BISHOP)
                            list += Move(r, c, one, nc, KNIGHT)
                        } else list += Move(r, c, one, nc)
                }
                val ep = st.enPassantTarget
                if (ep != null && ep.first == one && abs(ep.second - c) == 1)
                    list += Move(r, c, ep.first, ep.second, isEnPassant = true)
            }

            KNIGHT -> {
                val off = listOf(
                    Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
                )
                for (o in off) {
                    val nr = r + o.first;
                    val nc = c + o.second
                    if (nr in 0..7 && nc in 0..7) {
                        val t = st.board[nr][nc]
                        if (t == null || t.color != p.color) list += Move(r, c, nr, nc)
                    }
                }
            }

            BISHOP -> slide(
                r, c, st, list, listOf(
                    Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
                )
            )

            ROOK -> slide(
                r, c, st, list, listOf(
                    Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)
                )
            )

            QUEEN -> slide(
                r, c, st, list, listOf(
                    Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1),
                    Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)
                )
            )

            KING -> {
                for (dr in -1..1) for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val nr = r + dr;
                    val nc = c + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val t = st.board[nr][nc]
                        if (t == null || t.color != p.color) list += Move(r, c, nr, nc)
                    }
                }
                if (p.color == WHITE && r == 7 && c == 4) {
                    if (st.whiteCastleKingside && st.board[7][5] == null && st.board[7][6] == null)
                        if (!attacked(7, 4, BLACK, st.board)
                            && !attacked(7, 5, BLACK, st.board)
                            && !attacked(7, 6, BLACK, st.board)
                        )
                            list += Move(7, 4, 7, 6, isCastling = true)
                    if (st.whiteCastleQueenside && st.board[7][3] == null
                        && st.board[7][2] == null && st.board[7][1] == null
                    )
                        if (!attacked(7, 4, BLACK, st.board)
                            && !attacked(7, 3, BLACK, st.board)
                            && !attacked(7, 2, BLACK, st.board)
                        )
                            list += Move(7, 4, 7, 2, isCastling = true)
                }
                if (p.color == BLACK && r == 0 && c == 4) {
                    if (st.blackCastleKingside && st.board[0][5] == null && st.board[0][6] == null)
                        if (!attacked(0, 4, WHITE, st.board)
                            && !attacked(0, 5, WHITE, st.board)
                            && !attacked(0, 6, WHITE, st.board)
                        )
                            list += Move(0, 4, 0, 6, isCastling = true)
                    if (st.blackCastleQueenside && st.board[0][3] == null
                        && st.board[0][2] == null && st.board[0][1] == null
                    )
                        if (!attacked(0, 4, WHITE, st.board)
                            && !attacked(0, 3, WHITE, st.board)
                            && !attacked(0, 2, WHITE, st.board)
                        )
                            list += Move(0, 4, 0, 2, isCastling = true)
                }
            }
        }
        return list
    }

    private fun slide(
        r: Int,
        c: Int,
        st: GameState,
        res: MutableList<Move>,
        dirs: List<Pair<Int, Int>>
    ) {
        val p = st.board[r][c]!!
        for (d in dirs) {
            var nr = r + d.first;
            var nc = c + d.second
            while (nr in 0..7 && nc in 0..7) {
                val t = st.board[nr][nc]
                if (t == null) res += Move(r, c, nr, nc)
                else {
                    if (t.color != p.color) {
                        res += Move(r, c, nr, nc)
                    }
                    break
                }
                nr += d.first; nc += d.second
            }
        }
    }

    private fun attacked(
        r: Int,
        c: Int,
        by: PieceColor,
        board: Array<Array<Piece?>>
    ): Boolean {
        for (row in 0..7) for (col in 0..7) {
            val p = board[row][col] ?: continue
            if (p.color != by) continue
            when (p.type) {
                PAWN -> {
                    val dir = if (by == WHITE) -1 else 1
                    if (row + dir == r && abs(col - c) == 1) return true
                }

                KNIGHT -> if (
                    listOf(
                        Pair(-2, -1), Pair(-2, 1),
                        Pair(-1, -2), Pair(-1, 2),
                        Pair(1, -2), Pair(1, 2),
                        Pair(2, -1), Pair(2, 1)
                    ).any { row + it.first == r && col + it.second == c }
                ) return true

                BISHOP -> if (abs(row - r) == abs(col - c)
                    && clear(row, col, r, c, board)
                ) return true

                ROOK -> if ((row == r || col == c)
                    && clear(row, col, r, c, board)
                ) return true

                QUEEN -> if ((row == r || col == c
                            || abs(row - r) == abs(col - c))
                    && clear(row, col, r, c, board)
                ) return true

                KING -> if (
                    max(abs(row - r), abs(col - c)) == 1
                ) return true
            }
        }
        return false
    }

    private fun clear(fr: Int, fc: Int, tr: Int, tc: Int, b: Array<Array<Piece?>>): Boolean {
        val dr = (tr - fr).coerceIn(-1, 1)
        val dc = (tc - fc).coerceIn(-1, 1)
        var r = fr + dr;
        var c = fc + dc
        while (r != tr || c != tc) {
            if (b[r][c] != null) return false
            r += dr; c += dc
        }
        return true
    }

    private fun opposite(c: PieceColor) =
        if (c == WHITE) BLACK else WHITE

    fun reset() {
        history.clear()
        _state.value = GameState(
            board = initialBoard(),
            currentTurn = if (Random.nextBoolean()) WHITE else BLACK,
            whiteCastleKingside = true,
            whiteCastleQueenside = true,
            blackCastleKingside = true,
            blackCastleQueenside = true,
            enPassantTarget = null,
            selected = null,
            moves = emptyList(),
            status = GameStatus.RUNNING
        )
    }
}
