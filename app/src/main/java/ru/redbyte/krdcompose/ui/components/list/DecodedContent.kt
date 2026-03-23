package ru.redbyte.krdcompose.ui.components.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.redbyte.krdcompose.screens.list.hackerList.HackerListStyle
import ru.redbyte.krdcompose.screens.list.hackerList.innerShape
import kotlin.random.Random

@Composable
fun DecodedContent(
    expanded: Boolean,
    details: String,
    style: HackerListStyle,
    modifier: Modifier = Modifier
) {
    val palette = style.palette
    val animations = style.animations

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var showDecodedText by remember(expanded) { mutableStateOf(false) }

        LaunchedEffect(expanded) {
            if (expanded) {
                showDecodedText = false
                delay(animations.decodedTextRevealDelayMillis)
                showDecodedText = true
            } else {
                showDecodedText = false
            }
        }

        val scrambledText by produceState(
            initialValue = "",
            key1 = expanded,
            key2 = details
        ) {
            if (!expanded) {
                value = ""
                return@produceState
            }

            repeat(animations.decodeSteps) { step ->
                val revealFraction = (step + 1) / animations.decodeSteps.toFloat()
                value = scrambleRevealText(
                    target = details,
                    revealFraction = revealFraction
                )
                delay(animations.decodeStepDelayMillis)
            }

            value = details
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = palette.decodedPanelBackground,
                    shape = style.innerShape()
                )
                .padding(style.dimensions.innerCornerRadius),
            verticalArrangement = Arrangement.spacedBy(style.dimensions.itemSpacing / 2)
        ) {
            Text(
                text = "DECRYPTING PAYLOAD",
                color = palette.accent,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(color = palette.accentDim)

            Text(
                text = if (showDecodedText) details else scrambledText,
                color = if (showDecodedText) palette.primaryText else palette.secondaryText,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun scrambleRevealText(
    target: String,
    revealFraction: Float
): String {
    val randomChars = "01ABCDEF#@%&*+=-_/"
    val revealCount = (target.length * revealFraction).toInt()

    return buildString {
        target.forEachIndexed { index, char ->
            when {
                char.isWhitespace() -> append(char)
                index < revealCount -> append(char)
                else -> append(randomChars[Random.nextInt(randomChars.length)])
            }
        }
    }
}