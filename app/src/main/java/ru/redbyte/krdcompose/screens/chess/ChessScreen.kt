package ru.redbyte.krdcompose.screens.chess

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.ui.games.chess.ChessViewModel
import ru.redbyte.krdcompose.ui.games.chess.PieceColor
import ru.redbyte.krdcompose.ui.games.chess.PieceType

@Composable
fun ChessScreen(viewModel: ChessViewModel = viewModel()) {
    val state by viewModel.gameState.collectAsState()
    Column(
        Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
            .padding(16.dp)
    ) {
        for (row in 7 downTo 0) {
            Row {
                for (col in 0..7) {
                    val isLightSquare = (row + col) % 2 == 0
                    val color = if (isLightSquare) Color(0xFFEEEED2) else Color(0xFF769656)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color)
                            .border(
                                width = if (state.selected == Pair(row, col)) 2.dp else 0.dp,
                                color = Color.Yellow
                            )
                            .clickable { viewModel.onSquareClick(row, col) }
                    ) {
                        val piece = state.board[row][col]
                        if (piece != null) {
                            val imageRes = when (piece.type) {
                                PieceType.KING -> if (piece.color == PieceColor.WHITE) R.drawable.king_white else R.drawable.king_black
                                PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) R.drawable.queen_white else R.drawable.queen_black
                                PieceType.ROOK -> if (piece.color == PieceColor.WHITE) R.drawable.rook_white else R.drawable.rook_black
                                PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) R.drawable.bishop_white else R.drawable.bishop_black
                                PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) R.drawable.knight_white else R.drawable.knight_black
                                PieceType.PAWN -> if (piece.color == PieceColor.WHITE) R.drawable.pawn_white else R.drawable.pawn_black
                            }
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
        Text(text = "Ход: ${state.currentTurn}", modifier = Modifier.padding(top = 16.dp))
    }
}
