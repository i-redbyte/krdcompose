package ru.redbyte.krdcompose.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Рисует фрактальное «дерево Пифагора» на [`Canvas`][androidx.compose.ui.graphics.Canvas].
 *
 * Компонент поддерживает анимацию изменения угла между ветвями, запускаемую
 * одиночным нажатием на область рисования. Повторное нажатие останавливает
 * анимацию и возвращает дерево к начальному состоянию.
 *
 * @param modifier      Внешний [Modifier] — задаёт размеры, отступы и т.д.
 * @param levels        Глубина рекурсии (количество уровней дерева).
 *                      Чем больше значение, тем детальнее фрактал, но тем
 *                      выше нагрузка на GPU. Рекомендуемый диапазон — 5-13.
 * @param colors        Список цветов квадратов. Цвета циклически применяются
 *                      от корня к листьям. Если количество уровней превышает
 *                      размер списка, палитра повторяется.
 * @param startAngle    Начальный угол между левой и правой ветвью в градусах,
 *                      используемый до запуска анимации и после её остановки.
 * @param angleRange    Диапазон допустимых значений угла (`FloatRange`), внутри
 *                      которого происходит колебательная анимация.
 * @param durationMillis Длительность анимации полного прохода от минимального
 *                       до максимального значения угла и обратно.
 * @param centerVertically Если `true`, корневой квадрат размещается по центру
 *                         экрана по вертикали; иначе — «прижат» к нижнему краю
 *                         с небольшим отступом.
 * @param rootRotation  Поворот всего дерева вокруг корня (в градусах). Позволяет
 *                      получить наклонённое или «лежащие» дерево.
 *
 * **Поведение жестов**
 *
 * - Одно нажатие — переключение состояния анимации (старт/стоп).
 * - При остановке фрактал мгновенно возвращается к `startAngle`.
 *
 * **Примечания по производительности**
 *
 * - На устройствах с низкой графической мощностью избегайте значений
 *   `levels > 13` или уменьшайте размер канвы.
 * - Анимация реализована через [Animatable], что упрощает код, но при желании
 *   можно заменить на [InfiniteTransition] для более «декларативного» подхода.
 */
@Composable
fun PythagorasTree(
    modifier: Modifier = Modifier,
    levels: Int = 9,
    colors: List<Color> = listOf(Color(0xFF4CAF50)),
    startAngle: Float = 45f,
    angleRange: ClosedFloatingPointRange<Float> = 10f..60f,
    durationMillis: Int = 3000,
    centerVertically: Boolean = true,
    rootRotation: Float = 0f
) {
    val angle = remember { Animatable(startAngle) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Canvas(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    running = !running
                    if (running) {
                        scope.launch {
                            while (running) {
                                angle.animateTo(
                                    angleRange.endInclusive,
                                    tween(durationMillis, easing = LinearEasing)
                                )
                                angle.animateTo(
                                    angleRange.start,
                                    tween(durationMillis, easing = LinearEasing)
                                )
                            }
                        }
                    } else {
                        scope.launch { angle.snapTo(startAngle) }
                    }
                }
            }
    ) {
        val side = size.minDimension * 0.35f

        val rad = Math.toRadians(rootRotation.toDouble())
        val originX = size.width / 2f - cos(rad).toFloat() * side / 2f
        val originY = if (centerVertically)
            size.height / 2f + side / 2f
        else
            size.height - 16f

        val origin = Offset(originX, originY)
        drawTree(
            level = levels,
            p = origin,
            length = side,
            rotation = rootRotation,
            angle = angle.value,
            palette = colors
        )
    }
}

/**
 * Рекурсивно отрисовывает квадраты дерева Пифагора.
 *
 * @param level    Текущий уровень рекурсии; при достижении 0 функция завершает
 *                 отрисовку ветви.
 * @param p        Координаты левого-нижнего угла текущего квадрата.
 * @param length   Длина стороны квадрата.
 * @param rotation Угол поворота квадрата относительно оси X.
 * @param angle    Текущий «межветвевой» угол из [Animatable].
 * @param palette  Палитра квадратов, циклически применяемая по уровням.
 */
private fun DrawScope.drawTree(
    level: Int,
    p: Offset,
    length: Float,
    rotation: Float,
    angle: Float,
    palette: List<Color>
) {
    if (level <= 0 || length < 1f) return
    val rad = Math.toRadians(rotation.toDouble())
    val v = Offset((length * cos(rad)).toFloat(), (length * sin(rad)).toFloat())
    val w = Offset(-v.y, v.x)

    val path = Path().apply {
        moveTo(p.x, p.y)
        lineTo(p.x + v.x, p.y + v.y)
        lineTo(p.x + v.x + w.x, p.y + v.y + w.y)
        lineTo(p.x + w.x, p.y + w.y)
        close()
    }
    drawPath(path, palette[(level - 1) % palette.size])

    val aRad = Math.toRadians(angle.toDouble())
    val leftLen = length * cos(aRad).toFloat()
    val rightLen = length * sin(aRad).toFloat()

    val pLeft = Offset(p.x + w.x, p.y + w.y)
    drawTree(
        level = level - 1,
        p = pLeft,
        length = leftLen,
        rotation = rotation - angle,
        angle = angle,
        palette = palette
    )

    val pRight = Offset(
        pLeft.x + v.x * cos(aRad).toFloat(),
        pLeft.y + v.y * cos(aRad).toFloat()
    )
    drawTree(
        level = level - 1,
        p = pRight,
        length = rightLen,
        rotation = rotation + 90f - angle,
        angle = angle,
        palette = palette
    )
}
