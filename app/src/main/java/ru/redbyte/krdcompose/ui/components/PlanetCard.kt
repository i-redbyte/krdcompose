@file:Suppress("unused")

package ru.redbyte.krdcompose.ui.components


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Immutable
data class PlanetCardColors(
    val containerGradient: List<Color>,
    val contentColor: Color,
    val borderColor: Color = Color.White.copy(alpha = 0.12f),
    val glowColor: Color = Color.White.copy(alpha = 0.18f),
    val secondaryContentColor: Color = contentColor.copy(alpha = 0.72f),
    val badgeContainerColor: Color = Color.White.copy(alpha = 0.10f),
    val badgeContentColor: Color = contentColor,
)

@Immutable
data class PlanetVisualStyle(
    val colors: List<Color>,
    val glowColor: Color = Color.White.copy(alpha = 0.18f),
    val showRings: Boolean = false,
    val ringColor: Color = Color.White.copy(alpha = 0.30f),
    val ringTiltDegrees: Float = -18f,
    val showAtmosphere: Boolean = true,
    val atmosphereColor: Color = Color.White.copy(alpha = 0.15f),
    val showShadow: Boolean = true,
    val shadowColor: Color = Color.Black.copy(alpha = 0.22f),
    val showCraters: Boolean = false,
    val craterColor: Color = Color.Black.copy(alpha = 0.12f),
    val showStripes: Boolean = false,
    val stripeColor: Color = Color.White.copy(alpha = 0.10f),
)

@Immutable
data class PlanetCardStyle(
    val shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    val colors: PlanetCardColors,
    val border: BorderStroke? = null,
    val tonalElevation: Dp = 0.dp,
    val shadowElevation: Dp = 0.dp,
    val contentPadding: Dp = 20.dp,
    val minHeight: Dp = 180.dp,
    val starCount: Int = 18,
    val showStars: Boolean = true,
    val showGlow: Boolean = true,
)

@Immutable
data class PlanetAnimationSpec(
    val enableFloating: Boolean = true,
    val floatingAmplitude: Dp = 6.dp,
    val floatingDurationMillis: Int = 3400,
    val enableRotation: Boolean = true,
    val rotationDurationMillis: Int = 16000,
    val enableTwinklingStars: Boolean = true,
    val twinkleDurationMillis: Int = 2200,
)

@Immutable
data class PlanetCardBadge(
    val text: String,
)

@Composable
fun PlanetCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    description: String? = null,
    badge: PlanetCardBadge? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    style: PlanetCardStyle = PlanetCardDefaults.cardStyle(),
    planetStyle: PlanetVisualStyle = PlanetCardDefaults.planetStyle(),
    animationSpec: PlanetAnimationSpec = PlanetAnimationSpec(),
    planetSize: Dp = 112.dp,
    planetAlignment: Alignment = Alignment.CenterEnd,
    planetPadding: Dp = 16.dp,
    header: (@Composable RowScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val floatingTransition = rememberInfiniteTransition(label = "planetFloating")
    val floatShift by floatingTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationSpec.floatingDurationMillis,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatShift",
    )

    val rotationTransition = rememberInfiniteTransition(label = "planetRotation")
    val rotation by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationSpec.rotationDurationMillis),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    val twinkleTransition = rememberInfiniteTransition(label = "starTwinkle")
    val twinkle by twinkleTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationSpec.twinkleDurationMillis),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "twinkle",
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = style.minHeight)
            .then(clickableModifier),
        shape = style.shape,
        color = Color.Transparent,
        contentColor = style.colors.contentColor,
        tonalElevation = style.tonalElevation,
        shadowElevation = style.shadowElevation,
        border = style.border ?: BorderStroke(1.dp, style.colors.borderColor),
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(style.colors.containerGradient),
                    shape = style.shape,
                )
                .clip(style.shape)
                .drawBehind {
                    if (style.showGlow) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(style.colors.glowColor, Color.Transparent),
                                center = Offset(size.width * 0.82f, size.height * 0.28f),
                                radius = size.minDimension * 0.42f,
                            ),
                            size = size,
                            cornerRadius = CornerRadius(
                                style.shape.topStart.toPx(size, this),
                                style.shape.topStart.toPx(size, this)
                            ),
                            blendMode = BlendMode.Plus,
                        )
                    }
                }
                .padding(style.contentPadding),
        ) {
            if (style.showStars) {
                StarField(
                    modifier = Modifier.matchParentSize(),
                    starCount = style.starCount,
                    alpha = if (animationSpec.enableTwinklingStars) twinkle else 0.85f,
                )
            }

            PlanetDecoration(
                modifier = Modifier
                    .align(planetAlignment)
                    .padding(planetPadding)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = if (animationSpec.enableFloating) {
                                (animationSpec.floatingAmplitude.toPx() * floatShift).roundToInt()
                            } else 0,
                        )
                    },
                planetSize = planetSize,
                style = planetStyle,
                rotation = if (animationSpec.enableRotation) rotation else 0f,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (header != null || badge != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (badge != null) {
                            BadgeChip(
                                text = badge.text,
                                containerColor = style.colors.badgeContainerColor,
                                contentColor = style.colors.badgeContentColor,
                            )
                        }
                        if (header != null) {
                            if (badge != null) Spacer(modifier = Modifier.width(8.dp))
                            header()
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = style.colors.contentColor,
                    )

                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = style.colors.secondaryContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = style.colors.secondaryContentColor,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                content?.invoke(this)

                footer?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    it()
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun PlanetDecoration(
    planetSize: Dp,
    style: PlanetVisualStyle,
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.requiredSize(planetSize),
        contentAlignment = Alignment.Center,
    ) {
        if (style.showRings) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(style.ringTiltDegrees),
            ) {
                drawOval(
                    color = style.ringColor,
                    topLeft = Offset(x = -size.width * 0.02f, y = size.height * 0.34f),
                    size = Size(width = this.size.width * 1.04f, height = this.size.height * 0.32f),
                    style = Stroke(width = this.size.minDimension * 0.05f),
                )
                drawOval(
                    color = style.ringColor.copy(alpha = 0.45f),
                    topLeft = Offset(x = this.size.width * 0.04f, y = this.size.height * 0.38f),
                    size = Size(width = this.size.width * 0.92f, height = this.size.height * 0.22f),
                    style = Stroke(width = this.size.minDimension * 0.02f),
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation),
        ) {
            if (style.glowColor.alpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(style.glowColor, Color.Transparent),
                        center = Offset(size.width * 0.45f, size.height * 0.40f),
                        radius = size.minDimension * 0.72f,
                    ),
                    radius = size.minDimension * 0.60f,
                    center = center,
                    blendMode = BlendMode.Plus,
                )
            }

            drawCircle(
                brush = Brush.linearGradient(style.colors),
                radius = size.minDimension * 0.34f,
                center = center,
            )

            if (style.showStripes) {
                val planetRadius = size.minDimension * 0.34f
                for (i in -2..2) {
                    drawArc(
                        color = style.stripeColor,
                        startAngle = 195f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - planetRadius,
                            center.y - planetRadius + i * planetRadius * 0.18f
                        ),
                        size = Size(planetRadius * 2f, planetRadius * 1.2f),
                        style = Stroke(width = planetRadius * 0.10f, cap = StrokeCap.Round),
                    )
                }
            }

            if (style.showAtmosphere) {
                drawCircle(
                    color = style.atmosphereColor,
                    radius = size.minDimension * 0.36f,
                    center = center,
                    style = Stroke(width = size.minDimension * 0.03f),
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(
                        center.x - size.minDimension * 0.14f, center.y - size.minDimension * 0.16f
                    ),
                    radius = size.minDimension * 0.20f,
                ),
                radius = size.minDimension * 0.34f,
                center = center,
            )

            if (style.showCraters) {
                val craterRadius = size.minDimension * 0.045f
                listOf(
                    center + Offset(-size.minDimension * 0.10f, -size.minDimension * 0.05f),
                    center + Offset(size.minDimension * 0.08f, size.minDimension * 0.02f),
                    center + Offset(-size.minDimension * 0.03f, size.minDimension * 0.12f),
                ).forEachIndexed { index, craterCenter ->
                    drawCircle(
                        color = style.craterColor.copy(alpha = 0.10f + (index * 0.03f)),
                        radius = craterRadius + index * size.minDimension * 0.008f,
                        center = craterCenter,
                    )
                }
            }

            if (style.showShadow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, style.shadowColor),
                        center = Offset(
                            center.x + size.minDimension * 0.16f,
                            center.y + size.minDimension * 0.10f
                        ),
                        radius = size.minDimension * 0.34f,
                    ),
                    radius = size.minDimension * 0.34f,
                    center = center,
                )
            }
        }
    }
}

@Composable
private fun StarField(
    starCount: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    val stars = remember(starCount) {
        List(starCount) { index ->
            val x = ((index * 37) % 100) / 100f
            val y = ((index * 53 + 17) % 100) / 100f
            val radius = 1.2f + (index % 3)
            Triple(x, y, radius)
        }
    }

    Canvas(modifier = modifier.alpha(alpha)) {
        stars.forEachIndexed { index, (x, y, radius) ->
            val starCenter = Offset(size.width * x, size.height * y)
            drawCircle(
                color = Color.White.copy(alpha = 0.28f + (index % 4) * 0.12f),
                radius = radius,
                center = starCenter,
            )

            if (index % 5 == 0) {
                drawPoints(
                    points = listOf(
                        starCenter.copy(y = starCenter.y - 4f),
                        starCenter.copy(y = starCenter.y + 4f),
                        starCenter.copy(x = starCenter.x - 4f),
                        starCenter.copy(x = starCenter.x + 4f),
                    ),
                    pointMode = androidx.compose.ui.graphics.PointMode.Points,
                    color = Color.White.copy(alpha = 0.20f),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(2f),
                )
            }
        }
    }
}

object PlanetCardDefaults {

    @Composable
    fun cardStyle(
        colors: PlanetCardColors = marsCardColors(),
    ): PlanetCardStyle = PlanetCardStyle(colors = colors)

    fun planetStyle(
        colors: List<Color> = listOf(Color(0xFFF6B26B), Color(0xFFD96C3E), Color(0xFFA63D2F)),
        showRings: Boolean = false,
        showCraters: Boolean = false,
        showStripes: Boolean = false,
    ): PlanetVisualStyle = PlanetVisualStyle(
        colors = colors,
        showRings = showRings,
        showCraters = showCraters,
        showStripes = showStripes,
    )

    fun marsCardColors(): PlanetCardColors = PlanetCardColors(
        containerGradient = listOf(Color(0xFF1A1025), Color(0xFF2A1638), Color(0xFF3B1E34)),
        contentColor = Color(0xFFFFF4EB),
        glowColor = Color(0xFFFF9A62).copy(alpha = 0.20f),
    )

    fun earthCardColors(): PlanetCardColors = PlanetCardColors(
        containerGradient = listOf(Color(0xFF071A31), Color(0xFF0B2E4F), Color(0xFF11385C)),
        contentColor = Color(0xFFF2FAFF),
        glowColor = Color(0xFF4FC3F7).copy(alpha = 0.22f),
    )

    fun saturnCardColors(): PlanetCardColors = PlanetCardColors(
        containerGradient = listOf(Color(0xFF1E1521), Color(0xFF34222D), Color(0xFF5A4134)),
        contentColor = Color(0xFFFFF8F1),
        glowColor = Color(0xFFFFCC80).copy(alpha = 0.18f),
    )
}

@Composable
@Preview
fun PlanetCardExample(modifier: Modifier = Modifier) {
    PlanetCard(
        modifier = modifier,
        title = "Mars",
        subtitle = "The Red Frontier",
        description = "A flexible Compose card with slots, custom visuals, and optional ambient animation.",
        badge = PlanetCardBadge("Featured"),
        style = PlanetCardDefaults.cardStyle(colors = PlanetCardDefaults.marsCardColors()),
        planetStyle = PlanetCardDefaults.planetStyle(
            showCraters = true,
            colors = listOf(Color(0xFFF6B26B), Color(0xFFD96C3E), Color(0xFFA63D2F)),
        ),
        footer = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPill(label = "Distance", value = "225M km")
                MetricPill(label = "Day", value = "24.6h")
            }
        },
    )
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

private fun CornerBasedShape.toPx(
    size: Size, drawScope: androidx.compose.ui.graphics.drawscope.DrawScope
): Float {
    val outline =
        createOutline(size = size, layoutDirection = drawScope.layoutDirection, density = drawScope)
    return when (outline) {
        is androidx.compose.ui.graphics.Outline.Rounded -> outline.roundRect.topLeftCornerRadius.x
        else -> 0f
    }
}


