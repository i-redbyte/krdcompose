package ru.redbyte.krdcompose.ui.games.snake

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.ui.games.snake.RenderMode.*
import java.util.Random
import kotlin.math.roundToInt

@Composable
fun SnakeGame(
    isWrapWalls: Boolean = true,
    livesCount: Int = 3,
    borderBackgroundColor: Color = Color.Black,
    menuBackgroundColor: Color = Color(0xFF3F51B5),
    textColor: Color = Color.White,
    mode: RenderMode = EMOJI,
    @DrawableRes headImageRes: Int = R.drawable.ic_snake_head,
    @DrawableRes foodImageRes: Int = R.drawable.ic_food,
    @DrawableRes tailImageRes: Int = R.drawable.ic_snake_tail,
    headColor: Color = Color.Green,
    foodColor: Color = Color.Red,
    tailColor: Color = Color.Yellow,
    emojiHead: String = "\uD83D\uDC0D",                       // 🐍
    emojiFood: String = "\uD83C\uDF4E",                       // 🍎
    emojiTail: String = "\uD83C\uDF51",                       // 🍑
) {
    var snake by remember { mutableStateOf(listOf(Cell(10, 10))) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var food by remember { mutableStateOf(Cell(15, 15)) }
    var lives by remember { mutableIntStateOf(livesCount) }
    var score by remember { mutableIntStateOf(0) }
    var isGameStarted by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var wrapWalls by remember { mutableStateOf(isWrapWalls) }
    var canvasWidth by remember { mutableIntStateOf(0) }
    var canvasHeight by remember { mutableIntStateOf(0) }
    var isGameWon by remember { mutableStateOf(false) }
    var lastDirection by remember { mutableStateOf(Direction.RIGHT) }
    val context = LocalContext.current
    val headBitmap = remember(headImageRes) {
        ImageBitmap.imageResource(context.resources, headImageRes)
    }
    val foodBitmap = remember(foodImageRes) {
        ImageBitmap.imageResource(context.resources, foodImageRes)
    }
    val tailBitmap = remember(foodImageRes) {
        ImageBitmap.imageResource(context.resources, tailImageRes)
    }

    LaunchedEffect(isGameStarted) {
        if (isGameStarted && !isGameWon) {
            while (!isGameOver) {
                delay(200L)
                val head = snake.first()
                val newHead = when (direction) {
                    Direction.UP -> Cell(head.x, head.y - 1)
                    Direction.DOWN -> Cell(head.x, head.y + 1)
                    Direction.LEFT -> Cell(head.x - 1, head.y)
                    Direction.RIGHT -> Cell(head.x + 1, head.y)
                }
                lastDirection = direction
                if (canvasWidth == 0 || canvasHeight == 0) continue
                val columns = 20
                val cellSize = canvasWidth / columns
                if (cellSize == 0) continue
                val rows = (canvasHeight / cellSize)
                val totalCells = (columns * rows) - 1
                val nextHead = if (wrapWalls) {
                    Cell((newHead.x + columns) % columns, (newHead.y + rows) % rows)
                } else newHead
                val hitWall =
                    !wrapWalls && (newHead.x < 0 || newHead.y < 0 || newHead.x >= columns || newHead.y >= rows)
                if (snake.contains(nextHead) || hitWall) {
                    if (lives > 1) {
                        lives--
                        snake = listOf(Cell(10, 10))
                        direction = Direction.RIGHT
                        continue
                    } else {
                        isGameOver = true
                        isGameStarted = false
                        continue
                    }
                }
                if (nextHead == food) {
                    snake = listOf(nextHead) + snake
                    score++
                    if (snake.size == totalCells) {
                        isGameWon = true
                        isGameStarted = false
                        continue
                    }
                    val random = Random()
                    var newFood: Cell
                    do {
                        newFood = Cell(random.nextInt(columns), random.nextInt(rows))
                    } while (snake.contains(newFood))
                    food = newFood
                } else {
                    snake = listOf(nextHead) + snake.dropLast(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(menuBackgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Счёт: $score",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(32.dp))
            Text(
                "Жизни: $lives",
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Через стены", color = textColor)
                Spacer(Modifier.width(8.dp))
                Switch(checked = wrapWalls, onCheckedChange = { wrapWalls = it })
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(borderBackgroundColor)
                .onSizeChanged {
                    canvasWidth = it.width
                    canvasHeight = it.height
                }) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (dx, dy) = dragAmount
                            direction = when {
                                abs(dx) > abs(dy) -> {
                                    if (dx > 0 && lastDirection != Direction.LEFT) Direction.RIGHT
                                    else if (dx < 0 && lastDirection != Direction.RIGHT) Direction.LEFT
                                    else direction
                                }

                                else -> {
                                    if (dy > 0 && lastDirection != Direction.UP) Direction.DOWN
                                    else if (dy < 0 && lastDirection != Direction.DOWN) Direction.UP
                                    else direction
                                }
                            }

                        }
                    }) {
                val cellSize = (size.width / 20f)
                val cellSizeInt = cellSize.roundToInt()
                snake.drop(1).forEach { cell ->

                    when (mode) {
                        EMOJI -> {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    textSize = cellSize
                                }
                                drawText(
                                    emojiTail,
                                    cell.x * cellSize,
                                    cell.y * cellSize + (cellSize / 2 + paint.descent()),
                                    paint
                                )
                            }
                        }

                        IMAGE -> {
                            drawImage(
                                image = tailBitmap,
                                dstSize = IntSize(cellSizeInt, cellSizeInt),
                                dstOffset = IntOffset(
                                    cell.x * cellSize.roundToInt(),
                                    cell.y * cellSize.roundToInt()
                                )
                            )
                        }

                        SHAPE -> {
                            drawRect(
                                color = tailColor,
                                topLeft = Offset(cell.x * cellSize, cell.y * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
                snake.firstOrNull()?.let { head ->
                    when (mode) {
                        EMOJI -> {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    textSize = cellSize
                                }
                                drawText(
                                    emojiHead,
                                    head.x * cellSize,
                                    (head.y + 1) * cellSize - paint.descent(),
                                    paint
                                )
                            }
                            food.let { f ->
                                drawContext.canvas.nativeCanvas.apply {
                                    val paint = android.graphics.Paint().apply {
                                        textSize = cellSize
                                    }
                                    drawText(
                                        emojiFood,
                                        f.x * cellSize,
                                        (f.y + 1) * cellSize - paint.descent(),
                                        paint
                                    )
                                }
                            }
                        }

                        IMAGE -> {
                            drawImage(
                                image = headBitmap,
                                dstSize = IntSize(cellSizeInt, cellSizeInt),
                                dstOffset = IntOffset(
                                    (head.x * cellSize).roundToInt(),
                                    (head.y * cellSize).roundToInt()
                                )
                            )
                            drawImage(
                                image = foodBitmap,
                                dstSize = IntSize(cellSizeInt, cellSizeInt),
                                dstOffset = IntOffset(
                                    (food.x * cellSize).roundToInt(),
                                    (food.y * cellSize).roundToInt()
                                )
                            )
                        }

                        SHAPE -> {
                            snake.forEachIndexed { i, cell ->
                                drawRect(
                                    color = if (i == 0) headColor else tailColor,
                                    topLeft = Offset(cell.x * cellSize, cell.y * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                            drawRect(
                                color = foodColor,
                                topLeft = Offset(food.x * cellSize, food.y * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }
        when {
            isGameOver -> {
                Text(
                    "Вы проиграли",
                    color = Color.Red,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        isGameStarted = false
                        isGameOver = false
                        isGameWon = false
                        lives = 3
                        score = 0
                        snake = listOf(Cell(10, 10))
                        direction = Direction.RIGHT
                        food = Cell(15, 15)
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Начать заново")
                }
            }

            isGameWon -> {
                Text(
                    "Вы победили",
                    color = Color.Green,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        isGameStarted = false
                        isGameOver = false
                        isGameWon = false
                        lives = 3
                        score = 0
                        snake = listOf(Cell(10, 10))
                        direction = Direction.RIGHT
                        food = Cell(15, 15)
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Начать заново")
                }
            }

            !isGameStarted -> {
                Button(onClick = { isGameStarted = true }, modifier = Modifier.padding(16.dp)) {
                    Text("Начать")
                }
            }
        }
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }

enum class RenderMode { IMAGE, EMOJI, SHAPE }