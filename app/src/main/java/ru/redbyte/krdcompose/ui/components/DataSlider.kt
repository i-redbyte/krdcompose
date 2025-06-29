package ru.redbyte.krdcompose.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader.TileMode.DECAL
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.BLUR_INTENSITY_MULTIPLIER
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.BLUR_START_THRESHOLD
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.BLUR_THRESHOLD
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.CENTER_PIVOT_Y
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MAX_ALPHA
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MAX_BLUR_RADIUS
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MAX_ROTATION_DEGREES
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MAX_SCALE
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MIN_ALPHA
import ru.redbyte.krdcompose.ui.components.DataSliderDefaults.MIN_SCALE
import kotlin.math.absoluteValue

/**
 * ### DataSlider
 *
 * Универсальный свайпер-карусель, предназначенный для показа набора страниц-картинок с подписью.
 * Центральный элемент масштабируется и (опционально) слегка поворачивается, элементы по краям
 * — уменьшаются, отдаляются и размываются.
 * Работает поверх `HorizontalPager` из `accompanist-pager` и сохраняет все публичные
 * параметры для полной совместимости.
 *
 * #### Пример использования
 * ```kotlin
 * val pages = persistentListOf(
 *     DataPage({ Image(painterResource(R.drawable.pic1), null) }, "Первая"),
 *     DataPage({ Image(painterResource(R.drawable.pic2), null) }, "Вторая"),
 *     DataPage({ Image(painterResource(R.drawable.pic3), null) }, "Третья")
 * )
 *
 * DataSlider(
 *     pages = pages,
 *     enableSwipe = true,
 *     rotateImage = true,
 *     backgroundColor = MaterialTheme.colorScheme.background
 * )
 * ```
 *
 * @param pages            Неизменяемый список страниц. Каждая страница — это пара из контента
 *                         (любая `@Composable` функция, например `Image`) и подписи.
 * @param modifier         Внешний `Modifier`. Позволяет управлять отступами, клипом, размером и т. д.
 * @param enableSwipe      Если `false`, свайпы блокируются, и перелистывать можно только кодом
 *                         через `pagerState`.
 * @param rotateImage      Добавлять ли эффект лёгкого 3-D поворота центральному элементу.
 * @param backgroundColor  Цвет фона под слайдером и подписью.
 * @param pagerState       Состояние `PagerState`. Передайте собственное, если нужно
 *                         контролировать текущую страницу извне или синхронизировать несколько
 *                         компонентов.
 * @param onPageWidthCalc  Колбэк, сообщающий фактическую ширину страницы (с учётом spacing) в px.
 *                         Может быть полезен для синхронизации прокрутки кастомных индикаторов,
 *                         — оставьте пустым, если не нужен.
 *
 * @see DataPage           Модель страницы слайдера
 * @see rememberPagerState Чтобы создать управляющее состояние вне компонента
 */
@Composable
fun DataSlider(
    pages: ImmutableList<DataPage>,
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true,
    rotateImage: Boolean = true,
    backgroundColor: Color = Color.White,
    pagerState: PagerState = rememberPagerState { pages.size },
    onPageWidthCalc: (Float) -> Unit = {}
) {
    val sliderConfig = rememberSliderConfig()

    Column(
        modifier = modifier
            .padding(top = 0.dp)
            .clipToBounds()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PagerContent(
            pages = pages,
            pagerState = pagerState,
            enableSwipe = enableSwipe,
            rotateImage = rotateImage,
            sliderConfig = sliderConfig,
            onPageWidthCalc = onPageWidthCalc
        )

        CaptionText(
            pages = pages,
            pagerState = pagerState,
            sliderConfig = sliderConfig
        )

        Spacer(Modifier.height(sliderConfig.bottomSpacerHeight.dp))
    }
}

@Composable
private fun rememberSliderConfig(): DataSliderConfig {
    val windowInfo = LocalWindowInfo.current
    val screenWidth = with(LocalDensity.current) { windowInfo.containerSize.width.toDp() }

    return remember {
        DataSliderConfig(
            itemWidth = screenWidth * DataSliderDefaults.CENTRAL_ITEM_WIDTH_FRACTION,
            horizontalPadding = DataSliderDefaults.PEEK.dp + DataSliderDefaults.PAGE_SPACING.dp
        )
    }
}

@Composable
private fun PagerContent(
    pages: ImmutableList<DataPage>,
    pagerState: PagerState,
    enableSwipe: Boolean,
    rotateImage: Boolean,
    sliderConfig: DataSliderConfig,
    onPageWidthCalc: (Float) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp),
        userScrollEnabled = enableSwipe,
        contentPadding = PaddingValues(horizontal = sliderConfig.horizontalPadding),
        pageSpacing = DataSliderDefaults.PAGE_SPACING.dp,
        pageSize = PageSize.Fixed(sliderConfig.itemWidth)
    ) { page ->
        PagerItem(
            page = page,
            pages = pages,
            pagerState = pagerState,
            rotateImage = rotateImage,
            itemWidth = sliderConfig.itemWidth,
            onPageWidthCalc = onPageWidthCalc
        )
    }
}

@Composable
private fun PagerItem(
    page: Int,
    pages: ImmutableList<DataPage>,
    pagerState: PagerState,
    rotateImage: Boolean,
    itemWidth: Dp,
    onPageWidthCalc: (Float) -> Unit
) {
    val pivotOffsetPx = with(LocalDensity.current) { DataSliderDefaults.PIVOT_OFFSET.dp.toPx() }
    val spacingPx = with(LocalDensity.current) { DataSliderDefaults.PAGE_SPACING.dp.toPx() }

    Box(
        modifier = Modifier
            .padding(top = 0.dp)
            .width(itemWidth)
            .aspectRatio(DataSliderDefaults.IMAGE_ASPECT_RATIO)
            .onSizeChanged { size ->
                onPageWidthCalc(size.width + spacingPx)
            }
            .graphicsLayer {
                applyPageTransformation(
                    page = page,
                    pagerState = pagerState,
                    rotateImage = rotateImage,
                    pivotOffsetPx = pivotOffsetPx
                )
            }
    ) {
        pages[page].content()
    }
}

@Composable
private fun CaptionText(
    pages: ImmutableList<DataPage>,
    pagerState: PagerState,
    sliderConfig: DataSliderConfig
) {
    Box(
        modifier = Modifier
            .height(DataSliderDefaults.CAPTION_BOX_HEIGHT.dp)
            .fillMaxWidth()
            .padding(horizontal = sliderConfig.horizontalTextPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = pages[pagerState.currentPage].caption,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DataSliderDefaults.CAPTION_BOTTOM_PADDING.dp),
            style = TextStyle(
                color = Color(0xFF009688),
                fontWeight = FontWeight(500),
                fontSize = 22.sp,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = DataSliderDefaults.MAX_CAPTION_LINES
        )
    }
}

private fun GraphicsLayerScope.applyPageTransformation(
    page: Int,
    pagerState: PagerState,
    rotateImage: Boolean,
    pivotOffsetPx: Float
) {
    val rawOffset = pagerState.currentPage - page + pagerState.currentPageOffsetFraction
    val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1f)

    val scale = lerp(MIN_SCALE, MAX_SCALE, 1f - absOffset)
    val rotation = if (rotateImage) MAX_ROTATION_DEGREES * (1f - absOffset) else 0f
    val alphaValue = lerp(MIN_ALPHA, MAX_ALPHA, MAX_ALPHA - absOffset)

    val pivotRelative = pivotOffsetPx / size.width
    transformOrigin = TransformOrigin(
        calculatePivotX(rawOffset, pivotRelative),
        CENTER_PIVOT_Y
    )

    scaleX = scale
    scaleY = scale
    rotationZ = rotation
    alpha = alphaValue

    val blurRadius = calculateBlurEffect(rawOffset)
    if (SDK_INT >= VERSION_CODES.S && blurRadius > BLUR_THRESHOLD) {
        renderEffect = RenderEffect
            .createBlurEffect(blurRadius, blurRadius, DECAL)
            .asComposeRenderEffect()
    }
}

private fun calculateBlurEffect(offset: Float): Float =
    if (offset.absoluteValue > BLUR_START_THRESHOLD) {
        (offset.absoluteValue - BLUR_START_THRESHOLD)
            .coerceAtMost(1f) *
                MAX_BLUR_RADIUS *
                BLUR_INTENSITY_MULTIPLIER
    } else 0f

private fun calculatePivotX(offset: Float, pivotOffset: Float): Float = when {
    offset < 0f -> lerp(
        DataSliderDefaults.CENTER_PIVOT_X,
        DataSliderDefaults.LEFT_EDGE_PIVOT_X - pivotOffset,
        (-offset).coerceIn(0f, 1f)
    )

    offset > 0f -> lerp(
        DataSliderDefaults.CENTER_PIVOT_X,
        DataSliderDefaults.RIGHT_EDGE_PIVOT_X + DataSliderDefaults.RIGHT_PIVOT_OFFSET_PERCENT,
        offset.coerceIn(0f, 1f)
    )

    else -> DataSliderDefaults.CENTER_PIVOT_X
}

private data class DataSliderConfig(
    val itemWidth: Dp,
    val horizontalPadding: Dp,
    val horizontalTextPadding: Dp = DataSliderDefaults.HORIZONTAL_TEXT_PADDING.dp,
    val indicatorTopPadding: Int = DataSliderDefaults.INDICATOR_TOP_PADDING,
    val bottomSpacerHeight: Int = DataSliderDefaults.BOTTOM_SPACER_HEIGHT,
    val spacerHeight: Int = DataSliderDefaults.SPACER_HEIGHT,
)

data class DataPage(
    val content: @Composable () -> Unit,
    val caption: String
)

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private object DataSliderDefaults {
    /* Layout & padding */
    const val SPACER_HEIGHT = 8
    const val BOTTOM_SPACER_HEIGHT = 24
    const val PAGE_SPACING = 32
    const val PEEK = 16
    const val HORIZONTAL_TEXT_PADDING = 32
    const val PIVOT_OFFSET = 32

    /* Caption */
    const val CAPTION_BOX_HEIGHT = 88
    const val CAPTION_BOTTOM_PADDING = 16
    const val INDICATOR_TOP_PADDING = 12
    const val MAX_CAPTION_LINES = 3

    /* Transform */
    const val MIN_SCALE = 0.6f
    const val MAX_SCALE = 0.8f
    const val MAX_ROTATION_DEGREES = -45f
    const val MIN_ALPHA = 0.5f
    const val MAX_ALPHA = 1f

    /* Pivot */
    const val CENTER_PIVOT_X = 0.5f
    const val CENTER_PIVOT_Y = 0.5f
    const val RIGHT_EDGE_PIVOT_X = 1f
    const val LEFT_EDGE_PIVOT_X = 0f
    const val RIGHT_PIVOT_OFFSET_PERCENT = 0.1f

    /* Layout specifics */
    const val CENTRAL_ITEM_WIDTH_FRACTION = 0.75f
    const val IMAGE_ASPECT_RATIO = 3f / 4f

    /* Blur */
    const val BLUR_START_THRESHOLD = 0.8f
    const val MAX_BLUR_RADIUS = 12f
    const val BLUR_THRESHOLD = 0.5f
    const val BLUR_INTENSITY_MULTIPLIER = 6f
}
