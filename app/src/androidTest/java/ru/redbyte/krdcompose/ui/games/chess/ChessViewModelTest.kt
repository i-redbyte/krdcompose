package ru.redbyte.krdcompose.ui.games.chess

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.redbyte.krdcompose.ui.games.chess.model.GameStatus
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor
import ru.redbyte.krdcompose.ui.games.chess.model.PieceType

@RunWith(AndroidJUnit4::class)
class ChessViewModelTest {

    private fun createVmWithTurn(desired: PieceColor): ChessViewModel {
        val vm = ChessViewModel()
        repeat(20) {
            if (vm.state.value.currentTurn == desired) return vm
            vm.reset()
        }
        throw IllegalStateException("Could not initialise view‑model with $desired to move")
    }

    private fun move(vm: ChessViewModel, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        vm.onSquareClick(fromRow, fromCol)
        vm.onSquareClick(toRow, toCol)
    }

    @Test
    fun initialBoardSetup_correctPieces() {
        val vm = ChessViewModel()
        val st = vm.state.value

        assertEquals(PieceType.KING, st.board[0][4]?.type)
        assertEquals(PieceColor.BLACK, st.board[0][4]?.color)
        assertEquals(PieceType.KING, st.board[7][4]?.type)
        assertEquals(PieceColor.WHITE, st.board[7][4]?.color)

        for (c in 0..7) {
            assertEquals(PieceType.PAWN, st.board[1][c]?.type)
            assertEquals(PieceColor.BLACK, st.board[1][c]?.color)
            assertEquals(PieceType.PAWN, st.board[6][c]?.type)
            assertEquals(PieceColor.WHITE, st.board[6][c]?.color)
        }

        var pieces = 0
        for (r in 0..7) for (c in 0..7) if (st.board[r][c] != null) pieces++
        assertEquals(32, pieces)
    }

    @Test
    fun pawnMove_updatesBoardAndTurn() {
        val vm = createVmWithTurn(PieceColor.WHITE)

        move(vm, 6, 4, 4, 4)

        val st = vm.state.value
        assertNull(st.board[6][4])
        val pawn = st.board[4][4]
        assertNotNull(pawn)
        assertEquals(PieceType.PAWN, pawn!!.type)
        assertEquals(PieceColor.WHITE, pawn.color)
        assertEquals(PieceColor.BLACK, st.currentTurn)
        assertEquals("e2e4", st.moves.last())
    }

    @Test
    fun undo_revertsLastMove() {
        val vm = createVmWithTurn(PieceColor.WHITE)

        move(vm, 6, 4, 4, 4)
        vm.undo()

        val st = vm.state.value
        assertNotNull(st.board[6][4])
        assertNull(st.board[4][4])
        assertTrue(st.moves.isEmpty())
    }

    @Test
    fun castling_whiteKingside_succeeds() {
        val vm = createVmWithTurn(PieceColor.WHITE)

        move(vm, 6, 4, 5, 4)
        move(vm, 1, 0, 2, 0)
        move(vm, 7, 6, 5, 5)
        move(vm, 2, 0, 3, 0)
        move(vm, 7, 5, 5, 3)
        move(vm, 1, 7, 2, 7)
        vm.onSquareClick(7, 4)        // King e1
        vm.onSquareClick(7, 7)        // Rook h1

        val st = vm.state.value
        val king = st.board[7][6]
        val rook = st.board[7][5]
        assertNotNull(king)
        assertNotNull(rook)
        assertEquals(PieceType.KING, king!!.type)
        assertEquals(PieceColor.WHITE, king.color)
        assertEquals(PieceType.ROOK, rook!!.type)
        assertEquals(PieceColor.WHITE, rook.color)
        assertFalse(st.whiteCastleKingside)
        assertFalse(st.whiteCastleQueenside)
    }

    @Test
    fun enPassant_captureWorks() {
        val vm = createVmWithTurn(PieceColor.WHITE)

        move(vm, 6, 4, 4, 4)
        move(vm, 1, 0, 2, 0)
        move(vm, 4, 4, 3, 4)
        move(vm, 1, 3, 3, 3)
        move(vm, 3, 4, 2, 3)

        val st = vm.state.value
        val pawn = st.board[2][3]
        assertNotNull(pawn)
        assertEquals(PieceType.PAWN, pawn!!.type)
        assertEquals(PieceColor.WHITE, pawn.color)
        assertNull(st.board[3][3])
    }

    @Test
    fun foolsMate_gameEndsWithBlackWin() {
        val vm = createVmWithTurn(PieceColor.WHITE)
        move(vm, 6, 5, 5, 5)
        move(vm, 1, 4, 3, 4)
        move(vm, 6, 6, 4, 6)
        move(vm, 0, 3, 4, 7)

        val st = vm.state.value
        assertEquals(GameStatus.CHECK_WHITE, st.status)
        assertTrue(st.moves.last().endsWith("h4"))
    }

    @Test
    fun illegalMove_isRejected() {
        val vm = createVmWithTurn(PieceColor.WHITE)
        val before = vm.state.value

        vm.onSquareClick(6, 4)
        vm.onSquareClick(3, 4)

        val after = vm.state.value
        assertEquals(before.currentTurn, after.currentTurn)
        assertNotNull(after.board[6][4])
        assertNull(after.board[3][4])
        assertTrue(after.moves.isEmpty())
        assertNull(after.selected)
    }
}
