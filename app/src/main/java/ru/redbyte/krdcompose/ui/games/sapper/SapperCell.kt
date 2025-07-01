package ru.redbyte.krdcompose.ui.games.sapper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.ui.games.sapper.model.Cell

@Composable
internal fun SapperCell(
    cell: Cell,
    size: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isRevealed && cell.isMine -> Color.Red.copy(alpha = 0.5f)
        cell.isRevealed -> Color.LightGray
        else -> Color.DarkGray
    }
    val textColor = when (cell.neighborMines) {
        1 -> Color.Blue
        2 -> Color.Green
        3 -> Color.Red
        4 -> Color.Magenta
        5 -> Color(0xFF673AB7)
        6 -> Color.Cyan
        7 -> Color.Magenta
        8 -> Color.Gray
        else -> Color.Black

    }

    Box(
        modifier = Modifier
            .size(size)
            .border(1.dp, Color.Black)
            .background(backgroundColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            !cell.isRevealed -> {
                if (cell.isFlagged) {
                    Icon(
                        painterResource(R.drawable.ic_flag),
                        contentDescription = "Flag",
                        tint = Color.Yellow
                    )
                }
            }

            cell.isMine -> {
                Icon(
                    painterResource(R.drawable.ic_mine),
                    contentDescription = "Mine",
                    tint = Color.Black
                )
            }

            cell.neighborMines > 0 -> {
                Text(
                    text = "${cell.neighborMines}",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }

            else -> {
            }
        }
    }
}
