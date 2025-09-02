package ru.redbyte.krdcompose.screens.sudoku

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import ru.redbyte.krdcompose.games.sudoku.SudokuViewModel
import ru.redbyte.krdcompose.games.sudoku.model.CellContent
import ru.redbyte.krdcompose.games.sudoku.model.GameStatus
import kotlin.math.min

@Composable
fun SudokuGameScreen(viewModel: SudokuViewModel) {
    val generating by viewModel.generating
    val timeLeft by viewModel.timeRemaining
    val board = viewModel.board
    val notesList by viewModel.notesState
    val selectedCell by viewModel.selectedCell
    val noteMode by viewModel.noteMode
    val gameStatus by viewModel.gameStatus
    val isTimedGame by viewModel.isTimedGame
    val isCellEditable by viewModel.isCellEditableState

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        if (generating) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTimedGame) {
                    Text(
                        text = "Время: ${formatTime(timeLeft)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                SudokuBoard(
                    board = board,
                    notes = notesList,
                    selectedIndex = selectedCell,
                    isCellEditable = isCellEditable,
                    numberColors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF388E3C),
                        Color(0xFFF57C00),
                        Color(0xFF7B1FA2),
                        Color(0xFFFBC02D),
                        Color(0xFFE64A19),
                        Color(0xFF0097A7),
                        Color(0xFFD32F2F),
                        Color(0xFF303F9F)
                    ),
                    backgroundColor = Color.White,
                    gridLineColor = Color.Black,
                    onCellClick = { index -> viewModel.selectCell(index) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                SudokuControls(
                    onNumberClick = { num -> viewModel.inputNumber(num) },
                    onDelete = { viewModel.erase() },
                    onHint = { viewModel.useHint() },
                    onToggleNote = { viewModel.toggleNoteMode() },
                    noteMode = noteMode
                )
            }
        }

        if (gameStatus != GameStatus.Playing) {
            val message = if (gameStatus == GameStatus.Won) "Судоку решено!" else "Время вышло!"
            AlertDialog(
                onDismissRequest = {},
                title = { Text(text = "Игра окончена") },
                text = { Text(text = message) },
                confirmButton = {
                    Button(onClick = {
                        viewModel.startGame(
                            viewModel.currentDifficulty,
                            viewModel.isTimedGame.value
                        )
                    }) {
                        Text("Новая игра")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.exitToMenu() }) {
                        Text("Выйти")
                    }
                }
            )
        }
    }
}

@Composable
fun SudokuControls(
    onNumberClick: (Int) -> Unit,
    onDelete: () -> Unit,
    onHint: () -> Unit,
    onToggleNote: () -> Unit,
    noteMode: Boolean
) {
    val numberButtonSize = 56.dp
    val numberButtonShape = RoundedCornerShape(8.dp)
    val numberButtonColor = Color(0xFF1B5E20)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val numbers = (1..9).toList()
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until 3) {
                    val number = numbers[row * 3 + col]
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(numberButtonSize)
                            .clip(numberButtonShape)
                            .background(numberButtonColor)
                            .clickable { onNumberClick(number) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        val actionShape = RoundedCornerShape(8.dp)
        val actionColor = Color(0xFF1B5E20)
        val actionHeight = 48.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //TODO: Доработать
            ActionButton(
                text = if (noteMode) "Заметки: Вкл" else "Заметки: Выкл",
                onClick = onToggleNote,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                shape = actionShape,
                background = actionColor
            )
            ActionButton(
                text = "Стереть",
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                shape = actionShape,
                background = actionColor
            )
            ActionButton(
                text = "Подсказка",
                onClick = onHint,
                modifier = Modifier.weight(1f),
                height = actionHeight,
                shape = actionShape,
                background = actionColor
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    background: Color = Color(0xFF1B5E20)
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SudokuBoard(
    board: List<Int>,
    notes: List<Set<Int>>,
    selectedIndex: Int,
    isCellEditable: List<Boolean>,
    numberColors: List<Color>,
    backgroundColor: Color,
    gridLineColor: Color,
    onCellClick: (Int) -> Unit
) {

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cell = w / 9f
            for (i in 0..9) {
                val lw = if (i % 3 == 0) 4f else 1f
                drawLine(gridLineColor, Offset(i * cell, 0f), Offset(i * cell, h), lw)
            }
            for (j in 0..9) {
                val lw = if (j % 3 == 0) 4f else 1f
                drawLine(gridLineColor, Offset(0f, j * cell), Offset(w, j * cell), lw)
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(9), modifier = Modifier.matchParentSize()) {
            itemsIndexed(board, key = { idx, _ -> idx }) { index, cellValue ->
                val cellNotes = notes[index]
                val isSelected = index == selectedIndex
                val cellBg = if (isSelected) Color(0xFFE0E0E0) else Color.Transparent

                val contentState: CellContent = when {
                    cellValue in 1..9 -> CellContent.Value(cellValue)
                    cellNotes.isNotEmpty() -> CellContent.Notes(cellNotes)
                    else -> CellContent.Empty
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .background(cellBg)
                        .clickable(enabled = isCellEditable[index]) { onCellClick(index) }
                ) {
                    AnimatedContent(targetState = contentState, label = "cell") { state ->
                        when (state) {
                            is CellContent.Value -> {
                                val v = state.v
                                Text(
                                    text = v.toString(),
                                    color = numberColors.getOrElse(v - 1) { Color.Black },
                                    fontWeight = if (!isCellEditable[index]) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            is CellContent.Notes -> {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                ) {
                                    val cellW = size.width / 3f
                                    val cellH = size.height / 3f
                                    val textSize = min(cellW, cellH) * 0.6f

                                    val paint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        color = android.graphics.Color.DKGRAY
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        this.textSize = textSize
                                    }
                                    val fm = paint.fontMetrics
                                    val baselineShift = -(fm.ascent + fm.descent) / 2f

                                    for (num in 1..9) {
                                        if (num in state.set) {
                                            val r = (num - 1) / 3
                                            val c = (num - 1) % 3
                                            val cx = c * cellW + cellW / 2f
                                            val cy = r * cellH + cellH / 2f + baselineShift
                                            drawContext.canvas.nativeCanvas.drawText(
                                                num.toString(),
                                                cx,
                                                cy,
                                                paint
                                            )
                                        }
                                    }
                                }
                            }

                            CellContent.Empty -> Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}


//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun SudokuBoard(
//    board: List<Int>,
//    notes: List<Set<Int>>,
//    selectedIndex: Int,
//    isCellEditable: List<Boolean>,
//    numberColors: List<Color>,
//    backgroundColor: Color,
//    gridLineColor: Color,
//    onCellClick: (Int) -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .statusBarsPadding()
//            .aspectRatio(1f)
//            .fillMaxWidth()
//            .background(backgroundColor)
//    ) {
//        Canvas(modifier = Modifier.matchParentSize()) {
//            val w = size.width
//            val h = size.height
//            val cell = w / 9f
//            for (i in 0..9) {
//                val lw = if (i % 3 == 0) 4f else 1f
//                drawLine(gridLineColor, Offset(i * cell, 0f), Offset(i * cell, h), lw)
//            }
//            for (j in 0..9) {
//                val lw = if (j % 3 == 0) 4f else 1f
//                drawLine(gridLineColor, Offset(0f, j * cell), Offset(w, j * cell), lw)
//            }
//        }
//        LazyVerticalGrid(columns = GridCells.Fixed(9), modifier = Modifier.matchParentSize()) {
//            itemsIndexed(board, key = { idx, _ -> idx }) { index, cellValue ->
//                val cellNotes = notes[index]
//                val isSelected = index == selectedIndex
//                val cellBg = if (isSelected) Color(0xFFE0E0E0) else Color.Transparent
//                val contentState = remember(cellValue, cellNotes) {
//                    when {
//                        cellValue in 1..9 -> CellContent.Value(cellValue)
//                        cellNotes.isNotEmpty() -> CellContent.Notes(cellNotes)
//                        else -> CellContent.Empty
//                    }
//                }
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .aspectRatio(1f)
//                        .fillMaxWidth()
//                        .background(cellBg)
//                        .clickable(enabled = isCellEditable[index]) { onCellClick(index) }
//                ) {
//                    AnimatedContent(targetState = contentState, label = "cell") { state ->
//                        when (state) {
//                            is CellContent.Value -> {
//                                val v = state.v
//                                Text(
//                                    text = v.toString(),
//                                    color = numberColors.getOrElse(v - 1) { Color.Black },
//                                    fontWeight = if (!isCellEditable[index]) FontWeight.Bold else FontWeight.Normal,
//                                    fontSize = 20.sp,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                            is CellContent.Notes -> {
//                                Column(
//                                    verticalArrangement = Arrangement.Center,
//                                    horizontalAlignment = Alignment.CenterHorizontally,
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .padding(2.dp)
//                                ) {
//                                    for (nr in 0..2) {
//                                        Row(
//                                            horizontalArrangement = Arrangement.Center,
//                                            modifier = Modifier.fillMaxWidth()
//                                        ) {
//                                            for (nc in 0..2) {
//                                                val noteNum = nr * 3 + nc + 1
//                                                val present = state.set.contains(noteNum)
//                                                Text(
//                                                    text = if (present) noteNum.toString() else " ",
//                                                    fontSize = 8.sp,
//                                                    color = Color.DarkGray,
//                                                    textAlign = TextAlign.Center,
//                                                    modifier = Modifier.weight(1f)
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            CellContent.Empty -> Box(modifier = Modifier.fillMaxSize())
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}



