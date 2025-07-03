package ru.redbyte.krdcompose.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

/**
 * Callback, возвращающий текст для конкретной ячейки треугольника Паскаля.
 *
 * @param row   индекс строки (0–based)
 * @param col   индекс столбца внутри строки (0–based)
 * @param value вычисленное значение Паскаля в данной позиции
 */
typealias PascalCellTextProvider = (row: Int, col: Int, value: Int) -> String

/**
 * Регулярный шестиугольник, ориентированный «вершиной вверх».
 * Используется как shape для каждой ячейки.
 */
private val HexagonShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w / 2f, 0f)
    lineTo(w, h * 0.25f)
    lineTo(w, h * 0.75f)
    lineTo(w / 2f, h)
    lineTo(0f, h * 0.75f)
    lineTo(0f, h * 0.25f)
    close()
}

/**
 * Рендерит равносторонний треугольник Паскаля из шестиугольных ячеек.
 * Ячейки автоматически масштабируются так, чтобы самый длинный ряд умещался по
 * ширине контейнера. При pinch‑zoom весь треугольник масштабируется.
 *
 * ## Вложенность строк
 * При использовании шестиугольников высота ряда равна `0.75 × cellSize`, поэтому
 * строки «вкладываются» друг в друга без дополнительного зазора. Вертикальный шаг
 * задаётся так, чтобы центры соседних рядов совпадали с этим значением.
 *
 * ## Параметры
 * @param numRows      количество строк >= 1. Компонент перерисуется при изменении
 *                     этого параметра.
 * @param rowColors    список цветов для подсветки строк. Если цветов меньше, чем
 *                     строк, список циклически повторяется.
 * @param cellPadding  внешний отступ каждой ячейки; определяет горизонтальный и
 *                     вертикальный зазор между соседними гексами.
 * @param maxCellSize  максимальный размер шестиугольника. Если ряд не помещается,
 *                     размер будет автоматически уменьшен, но не превысит этот
 *                     предел.
 * @param cellText     функция, возвращающая текст для отображения в ячейке.
 * @param onCellClick  callback при нажатии на ячейку: возвращает `(row, col, value)`.
 * @param modifier     внешний `Modifier` для контейнера.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PascalTriangle(
    numRows: Int,
    rowColors: List<Color>,
    cellPadding: Dp = 1.dp,
    maxCellSize: Dp = 80.dp,
    cellText: PascalCellTextProvider = { _, _, v -> v.toString() },
    onCellClick: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val triangle = remember(numRows) { generatePascal(numRows) }
    var scale by remember { mutableFloatStateOf(1f) }

    BoxWithConstraints(modifier = modifier) {
        val containerWidth = maxWidth
        val cellSize = remember(containerWidth, numRows, cellPadding, maxCellSize) {
            val gaps = cellPadding * 2 * numRows
            val raw = (containerWidth - gaps) / numRows
            min(maxCellSize, raw)
        }
        val rowStep = -cellSize / 4
        Column(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(rowStep)
        ) {
            triangle.forEachIndexed { rowIndex, rowValues ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    rowValues.forEachIndexed { colIndex, value ->
                        val color = if (rowColors.isNotEmpty()) {
                            rowColors[rowIndex % rowColors.size]
                        } else MaterialTheme.colorScheme.primary

                        HexagonCell(
                            text = cellText(rowIndex, colIndex, value),
                            backgroundColor = color,
                            sizeDp = cellSize,
                            padding = cellPadding,
                            onClick = { onCellClick(rowIndex, colIndex, value) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Отдельная шестиугольная ячейка с анимацией «прижатия» при нажатии.
 *
 * @param text            отображаемый текст
 * @param backgroundColor цвет фона гексагона
 * @param onClick         callback при нажатии
 * @param sizeDp          размер стороны гексагона (ширина)
 * @param padding         внешний отступ вокруг ячейки
 */
@Composable
private fun HexagonCell(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    sizeDp: Dp,
    padding: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(padding)
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(backgroundColor, HexagonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Создаёт список списков с коэффициентами треугольника Паскаля.
 *
 * @param rows количество строк (>=1)
 * @return список строк, где каждая строка — это List<Int>.
 */
private fun generatePascal(rows: Int): List<List<Int>> {
    if (rows <= 0) return emptyList()
    val triangle = mutableListOf<List<Int>>()
    triangle.add(listOf(1))
    for (i in 1 until rows) {
        val prev = triangle[i - 1]
        val row = MutableList(i + 1) { 1 }
        for (j in 1 until i) row[j] = prev[j - 1] + prev[j]
        triangle.add(row)
    }
    return triangle
}