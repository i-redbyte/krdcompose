package ru.redbyte.krdcompose.screens.list.hackerList

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.redbyte.krdcompose.screens.list.hackerList.HackerPalette
import ru.redbyte.krdcompose.ui.components.list.DecodedContent
import ru.redbyte.krdcompose.ui.components.list.ScanHighlight
import kotlin.text.get

@Composable
fun HackerListScreen(
    style: HackerListStyle = HackerListStyles.MatrixGreen
) {
    val items: List<HackerNodeUi> = remember {
        HackerNodeFactory.createList()
    }

    var selectedId by remember { mutableIntStateOf(-1) }
    val expandedIds = remember { mutableStateListOf<Int>() }
    val scanTriggers = remember { mutableStateMapOf<Int, Int>() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = style.palette.screenBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(style.palette.screenBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = style.dimensions.screenHorizontalPadding,
                    vertical = style.dimensions.screenVerticalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(style.dimensions.itemSpacing)
            ) {
                item {
                    Header(style = style)
                }

                items(
                    items = items,
                    key = { it.id }
                ) { node ->
                    val selected = selectedId == node.id
                    val expanded = expandedIds.contains(node.id)
                    val scanTrigger = scanTriggers[node.id] ?: 0

                    HackerNodeCard(
                        item = node,
                        selected = selected,
                        expanded = expanded,
                        scanTrigger = scanTrigger,
                        style = style,
                        onClick = {
                            selectedId = node.id
                            scanTriggers[node.id] = (scanTriggers[node.id] ?: 0) + 1

                            if (expanded) {
                                expandedIds.remove(node.id)
                            } else {
                                expandedIds.add(node.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    style: HackerListStyle
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(style.dimensions.cardCornerRadius))
            .background(style.palette.cardBackground.copy(alpha = 0.8f))
            .padding(style.dimensions.screenHorizontalPadding)
    ) {
        Text(
            text = "SYSTEM NODES",
            color = style.palette.accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap a node to scan and decode details",
            color = style.palette.secondaryText,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = style.palette.accentDim)
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = style.palette.accent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NETWORK STABLE",
                color = style.palette.primaryText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HackerNodeCard(
    item: HackerNodeUi,
    selected: Boolean,
    expanded: Boolean,
    scanTrigger: Int,
    style: HackerListStyle,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val cardColor by animateColorAsState(
        targetValue = if (selected) style.palette.cardSelectedBackground else style.palette.cardBackground,
        animationSpec = tween(style.animations.cardColorDurationMillis),
        label = "cardColor"
    )

//    ScanHighlight(
//        scanTrigger = scanTrigger,
//        selected = selected,
//        style = style,
//        modifier = Modifier.fillMaxWidth()
//    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor, shape = style.cardShape())
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(style.dimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = style.palette.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.shortInfo,
                    color = style.palette.secondaryText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            StatusBadge(
                status = item.status,
                style = style
            )
        }

        HorizontalDivider(color = style.palette.accentDim)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> STATUS:",
                color = style.palette.accent,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.status,
                color = style.palette.primaryText,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        DecodedContent(
            expanded = expanded,
            details = item.details,
            style = style
        )
    }
//    }
}

@Composable
private fun StatusBadge(
    status: String,
    style: HackerListStyle
) {
    val badgeColor = when (status) {
        "ONLINE", "SYNCED" -> style.palette.accent
        "SCANNING", "WATCHING" -> style.palette.warning
        "LOCKED" -> style.palette.danger
        else -> style.palette.secondaryText
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusDot(color = badgeColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .width(8.dp)
            .height(8.dp)
    )
}