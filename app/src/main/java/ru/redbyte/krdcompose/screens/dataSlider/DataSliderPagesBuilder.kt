package ru.redbyte.krdcompose.screens.dataSlider

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import ru.redbyte.krdcompose.R
import ru.redbyte.krdcompose.ui.components.DataPage

internal class DataSliderPagesBuilder {
    private val pages = mutableListOf<DataPage>()

    @Composable
    fun pageOne(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android1),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageTwo(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android2),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageThree(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android3),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageFour(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android4),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageFive(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android5),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageSix(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android6),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    @Composable
    fun pageSeven(
        text: String
    ): DataSliderPagesBuilder {
        pages.add(
            DataPage(
                {
                    Image(
                        painterResource(R.drawable.logo_android7),
                        null,
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }, caption = text
            )
        )
        return this
    }

    fun build() = persistentListOf(*pages.toTypedArray())
}

@Composable
internal inline fun DataSliderPagesBuilder.composableApply(
    crossinline block: @Composable DataSliderPagesBuilder.() -> Unit
): DataSliderPagesBuilder {
    block()
    return this
}

@Composable
internal fun makePages(
    builder: @Composable DataSliderPagesBuilder.() -> Unit = {}
) = DataSliderPagesBuilder().composableApply(builder).build()

