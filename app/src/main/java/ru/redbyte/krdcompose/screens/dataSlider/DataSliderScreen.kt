package ru.redbyte.krdcompose.screens.dataSlider

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import ru.redbyte.krdcompose.ui.components.DataSlider

@Composable
fun DataSliderScreen() {
    val pages = makePages {
        pageOne("Очень интересный текст")
        pageTwo("Так интересно, что хочется читать и читать и читать и читать и читать и читать")
        pageThree("здесь могла быть ваша реклама")
        pageFour("Текст")
        pageFive("Снова текст")
        pageSix("Текс про андроид")
        pageSeven("The End")
    }
    val pagerState = rememberPagerState { pages.size }
    var pagerWidthPx by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        DataSlider(
            pages = pages,
            enableSwipe = true,
            pagerState = pagerState,
            onPageWidthCalc = { pagerWidthPx = it}
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollBy(
                            -pagerWidthPx,
                            animationSpec = tween(easing = LinearEasing)
                        )
                    }
                },
                enabled = pagerState.currentPage > 0
            ) { Text("Назад") }
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollBy(
                            pagerWidthPx,
                            animationSpec = tween(easing = LinearEasing)
                        )
                    }
                },
                enabled = pagerState.currentPage < pages.size - 1
            ) { Text("Вперед") }
        }
    }
}