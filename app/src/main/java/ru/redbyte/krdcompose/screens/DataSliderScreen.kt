package ru.redbyte.krdcompose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.redbyte.krdcompose.ui.components.DataSlider

@Composable
fun DataSliderScreen(/*navController: NavController*/) {
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
    val pagerWidthPx by remember { mutableFloatStateOf(0f) }
    Column(modifier = Modifier.fillMaxSize()) {
        DataSlider(
            pages = pages,
            pagerState = pagerState,
            onPageWidthCalc = { pagerWidthPx }
        )
    }
}