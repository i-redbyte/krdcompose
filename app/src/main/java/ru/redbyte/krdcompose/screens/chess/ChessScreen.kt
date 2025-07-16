package ru.redbyte.krdcompose.screens.chess

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.ui.games.chess.ChessViewModel
import ru.redbyte.krdcompose.ui.games.chess.model.GameState
import ru.redbyte.krdcompose.ui.games.chess.model.GameStatus.*
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor.BLACK
import ru.redbyte.krdcompose.ui.games.chess.model.PieceColor.WHITE
import ru.redbyte.krdcompose.ui.games.chess.model.PieceType
import ru.redbyte.krdcompose.ui.games.chess.model.PieceType.KING

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ChessScreen(viewModel: ChessViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Шахматы", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(
                            painterResource(R.drawable.ic_undo),
                            contentDescription = "Отменить ход"
                        )
                    }
                }
            )
        }
    ) { pad ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))
            BoardView(state, onSquare = { r, c -> viewModel.onSquareClick(r, c) })
            MovesList(state.moves)
        }

        if (state.status == MATE_WHITE_WINS ||
            state.status == MATE_BLACK_WINS ||
            state.status == STALEMATE
        ) {
            val text = when (state.status) {
                MATE_WHITE_WINS -> "Мат! Победили белые."
                MATE_BLACK_WINS -> "Мат! Победили чёрные."
                else -> "Пат. Ничья."
            }
            AlertDialog(
                onDismissRequest = { },
                title = { Text(text, fontWeight = FontWeight.Bold) },
                confirmButton = {
                    TextButton(onClick = { viewModel.reset() }) {
                        Text("Новая партия")
                    }
                }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BoardView(state: GameState, onSquare: (Int, Int) -> Unit) {
    val files = "abcdefgh"
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val sq = maxWidth / 10
        Column {
            FileRow(sq, files)
            for (r in 0..7) {
                Row {
                    RankCell(8 - r, sq)
                    for (c in 0..7) {
                        val light = (r + c) % 2 == 0
                        val bg = if (light) Color(0xFFEEEED2) else Color(0xFF769656)

                        val piece = state.board[r][c]
                        val isCheckedKing = (
                                (state.status == CHECK_WHITE)
                                        && (piece?.type == KING)
                                        && (piece.color == WHITE)) || (
                                (state.status == CHECK_BLACK)
                                        && (piece?.type == KING)
                                        && (piece.color == BLACK)
                                )
                        val isSelected =
                            state.selected?.let { it.first == r && it.second == c } ?: false
                        val borderColor = when {
                            isCheckedKing -> Color.Red
                            isSelected -> Color.Yellow
                            else -> null
                        }

                        Box(
                            modifier = Modifier
                                .size(sq)
                                .background(bg)
                                .then(borderColor?.let {
                                    Modifier.border(BorderStroke(3.dp, it))
                                }
                                    ?: Modifier)
                                .clickable { onSquare(r, c) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (piece != null) {
                                val color = piece.color
                                val res = when (piece.type) {
                                    KING -> if (color == WHITE) R.drawable.king_white
                                    else R.drawable.king_black

                                    PieceType.QUEEN -> if (color == WHITE) R.drawable.queen_white
                                    else R.drawable.queen_black

                                    PieceType.ROOK -> if (color == WHITE) R.drawable.rook_white
                                    else R.drawable.rook_black

                                    PieceType.BISHOP -> if (color == WHITE) R.drawable.bishop_white
                                    else R.drawable.bishop_black

                                    PieceType.KNIGHT -> if (color == WHITE) R.drawable.knight_white
                                    else R.drawable.knight_black

                                    PieceType.PAWN -> if (color == WHITE) R.drawable.pawn_white
                                    else R.drawable.pawn_black
                                }
                                Image(painter = painterResource(res), contentDescription = null)
                            }
                        }
                    }
                    RankCell(8 - r, sq)
                }
            }
            FileRow(sq, files)
        }
    }
}

@Composable
fun FileRow(sq: androidx.compose.ui.unit.Dp, files: String) {
    Row(Modifier.height(sq)) {
        Spacer(Modifier.width(sq))
        for (c in 0..7) {
            Box(Modifier.size(sq), contentAlignment = Alignment.Center) {
                Text(files[c].toString())
            }
        }
        Spacer(Modifier.width(sq))
    }
}

@Composable
fun RankCell(number: Int, sq: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(sq), contentAlignment = Alignment.Center) {
        Text(number.toString())
    }
}

@Composable
fun MovesList(moves: List<String>) {
    val pairs = moves.chunked(2)
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp)
            .padding(horizontal = 8.dp)
    ) {
        itemsIndexed(pairs) { idx, pair ->
            val white = pair.getOrNull(0) ?: ""
            val black = pair.getOrNull(1) ?: ""
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${idx + 1}.",
                    modifier = Modifier.width(28.dp),
                    fontSize = 14.sp
                )
                Text(
                    text = white,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
                Text(
                    text = black,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
            }
        }
    }
}