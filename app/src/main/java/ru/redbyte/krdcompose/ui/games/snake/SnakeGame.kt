package ru.redbyte.krdcompose.ui.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

enum class Direction { UP, DOWN, LEFT, RIGHT }
enum class WallCollisionMode { PASS_THROUGH, DIE }

@Composable
fun SnakeGame() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val cellSize = 20.dp
    val cellSizePx = with(LocalDensity.current) { cellSize.toPx() }

    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var isGamePaused by remember { mutableStateOf(false) }
    var wallCollisionMode by remember { mutableStateOf(WallCollisionMode.PASS_THROUGH) }

    val gridWidth = (screenWidth / cellSize).toInt()
    val gridHeight = ((screenHeight * 0.8f) / cellSize).toInt()

    var snake by remember {
        mutableStateOf(
            listOf(
                Offset(gridWidth / 2f, gridHeight / 2f),
                Offset(gridWidth / 2f - 1, gridHeight / 2f),
                Offset(gridWidth / 2f - 2, gridHeight / 2f)
            )
        )
    }

    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var nextDirection by remember { mutableStateOf(Direction.RIGHT) }

    var food by remember {
        mutableStateOf(
            generateFood(snake, gridWidth, gridHeight)
        )
    }

    fun loseLife() {
        lives--
        if (lives <= 0) {
            isGameOver = true
        } else {
            snake = listOf(
                Offset(gridWidth / 2f, gridHeight / 2f),
                Offset(gridWidth / 2f - 1, gridHeight / 2f),
                Offset(gridWidth / 2f - 2, gridHeight / 2f)
            )
            direction = Direction.RIGHT
            nextDirection = Direction.RIGHT
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            if (!isGamePaused && !isGameOver) {
                delay(150)
                direction = nextDirection
                val newHead = calculateNewHead(snake.first(), direction, gridWidth, gridHeight)

                if (wallCollisionMode == WallCollisionMode.DIE &&
                    (newHead.x < 0 || newHead.y < 0 || newHead.x >= gridWidth || newHead.y >= gridHeight)
                ) {
                    loseLife()
                } else {
                    val wrappedHead = if (wallCollisionMode == WallCollisionMode.PASS_THROUGH) {
                        Offset(
                            (newHead.x + gridWidth) % gridWidth,
                            (newHead.y + gridHeight) % gridHeight
                        )
                    } else newHead

                    if (snake.contains(wrappedHead)) {
                        loseLife()
                    } else {
                        val newSnake = if (wrappedHead == food) {
                            score++
                            food = generateFood(snake, gridWidth, gridHeight)
                            listOf(wrappedHead) + snake
                        } else {
                            listOf(wrappedHead) + snake.dropLast(1)
                        }
                        snake = newSnake
                    }
                }
            }
        }
    }

    fun resetGame() {
        score = 0
        lives = 3
        isGameOver = false
        snake = listOf(
            Offset(gridWidth / 2f, gridHeight / 2f),
            Offset(gridWidth / 2f - 1, gridHeight / 2f),
            Offset(gridWidth / 2f - 2, gridHeight / 2f)
        )
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        food = generateFood(snake, gridWidth, gridHeight)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp -> if (direction != Direction.DOWN) nextDirection =
                            Direction.UP

                        Key.DirectionDown -> if (direction != Direction.UP) nextDirection =
                            Direction.DOWN

                        Key.DirectionLeft -> if (direction != Direction.RIGHT) nextDirection =
                            Direction.LEFT

                        Key.DirectionRight -> if (direction != Direction.LEFT) nextDirection =
                            Direction.RIGHT

                        Key.Spacebar -> isGamePaused = !isGamePaused
                        else -> Unit
                    }
                    true
                } else false
            }
            .pointerInput(Unit) { // Добавляем обработку свайпов
                detectDragGestures { _, dragAmount ->
                    val (x, y) = dragAmount
                    when {
                        abs(x) > abs(y) -> { // Горизонтальный свайп
                            if (x > 0 && direction != Direction.LEFT) nextDirection =
                                Direction.RIGHT
                            else if (x < 0 && direction != Direction.RIGHT) nextDirection =
                                Direction.LEFT
                        }

                        else -> { // Вертикальный свайп
                            if (y > 0 && direction != Direction.UP) nextDirection = Direction.DOWN
                            else if (y < 0 && direction != Direction.DOWN) nextDirection =
                                Direction.UP
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Score: $score",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "Lives: $lives",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Draw food
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(food.x * cellSizePx, food.y * cellSizePx),
                        size = Size(cellSizePx, cellSizePx)
                    )

                    // Draw snake
                    snake.forEach { segment ->
                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(segment.x * cellSizePx, segment.y * cellSizePx),
                            size = Size(cellSizePx, cellSizePx)
                        )
                    }
                }

                if (isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Game Over!",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { resetGame() },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Play Again")
                            }
                        }
                    }
                }

                if (isGamePaused && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Paused",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = {
                    wallCollisionMode = if (wallCollisionMode == WallCollisionMode.PASS_THROUGH) {
                        WallCollisionMode.DIE
                    } else {
                        WallCollisionMode.PASS_THROUGH
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Wall Mode: ${wallCollisionMode.name.replace("_", " ")}",
                    fontSize = 16.sp
                )
            }
        }
    }
}

private fun calculateNewHead(
    head: Offset,
    direction: Direction,
    gridWidth: Int,
    gridHeight: Int
): Offset {
    return when (direction) {
        Direction.UP -> Offset(head.x, head.y - 1)
        Direction.DOWN -> Offset(head.x, head.y + 1)
        Direction.LEFT -> Offset(head.x - 1, head.y)
        Direction.RIGHT -> Offset(head.x + 1, head.y)
    }
}

private fun generateFood(snake: List<Offset>, gridWidth: Int, gridHeight: Int): Offset {
    val availablePositions = mutableListOf<Offset>()
    for (x in 0 until gridWidth) {
        for (y in 0 until gridHeight) {
            val position = Offset(x.toFloat(), y.toFloat())
            if (!snake.contains(position)) {
                availablePositions.add(position)
            }
        }
    }
    return availablePositions.random()
}