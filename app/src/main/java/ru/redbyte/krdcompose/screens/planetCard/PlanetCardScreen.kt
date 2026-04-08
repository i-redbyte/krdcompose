package ru.redbyte.krdcompose.screens.planetCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.ui.components.MetricPill
import ru.redbyte.krdcompose.ui.components.PlanetAnimationSpec
import ru.redbyte.krdcompose.ui.components.PlanetCard
import ru.redbyte.krdcompose.ui.components.PlanetCardBadge
import ru.redbyte.krdcompose.ui.components.PlanetCardColors
import ru.redbyte.krdcompose.ui.components.PlanetCardDefaults
import ru.redbyte.krdcompose.ui.components.PlanetCardStyle
import ru.redbyte.krdcompose.ui.components.PlanetVisualStyle

@Composable
fun PlanetCardScreen(
    modifier: Modifier = Modifier,
) {
    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060814),
            Color(0xFF0B1020),
            Color(0xFF11182B),
        ),
    )
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 20.dp, vertical = 28.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Демонстрация карточек планет",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Text(
            text = "Переиспользуемые Jetpack Compose-карточки с гибкой настройкой визуала, слотами и анимациями.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.72f),
        )

        PlanetCard(
            title = "Земля",
            subtitle = "Голубая планета",
            description = "Сбалансированный пресет для образовательных экранов, дашбордов или онбординга.",
            badge = PlanetCardBadge("Активно"),
            style = PlanetCardDefaults.cardStyle(colors = PlanetCardDefaults.earthCardColors()),
            planetStyle = PlanetCardDefaults.planetStyle(
                colors = listOf(
                    Color(0xFF6FE8FF),
                    Color(0xFF2D9CDB),
                    Color(0xFF1C4E80),
                ),
            ),
            footer = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricPill(label = "Пригодность", value = "100%")
                    MetricPill(label = "Спутники", value = "1")
                }
            },
        )

        PlanetCard(
            title = "Марс",
            subtitle = "Красная граница",
            description = "Более насыщенный вариант карточки с метриками внизу и визуалом с кратерами.",
            badge = PlanetCardBadge("Главная"),
            style = PlanetCardDefaults.cardStyle(colors = PlanetCardDefaults.marsCardColors()),
            planetStyle = PlanetCardDefaults.planetStyle(
                colors = listOf(Color(0xFFF6B26B), Color(0xFFD96C3E), Color(0xFFA63D2F)),
                showCraters = true,
            ),
            footer = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricPill(label = "Расстояние", value = "225 млн км")
                    MetricPill(label = "Сутки", value = "24.6 ч")
                }
            },
        )

        PlanetCard(
            title = "Сатурн",
            subtitle = "Кольцевой гигант",
            description = "Пример с кольцами, полосами и кастомным содержимым через slot API.",
            badge = PlanetCardBadge("Миссия"),
            style = PlanetCardDefaults.cardStyle(colors = PlanetCardDefaults.saturnCardColors()),
            planetStyle = PlanetCardDefaults.planetStyle(
                colors = listOf(
                    Color(0xFFF5D6A1),
                    Color(0xFFD9A45D),
                    Color(0xFF9F6C38),
                ),
                showRings = true,
                showStripes = true,
            ),
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricPill(label = "Кольца", value = "7")
                    MetricPill(label = "Спутники", value = "274")
                }
            },
            animationSpec = PlanetAnimationSpec(
                enableFloating = true,
                enableRotation = true,
                enableTwinklingStars = true,
                floatingAmplitude = 8.dp,
            ),
        )

        PlanetCard(
            title = "Андроид-Краснодар",
            subtitle = "Зелёная планета технологий",
            description = "Посвящается самому лучшему IT-сообществу города!",
            badge = PlanetCardBadge("KRD"),
            style = PlanetCardStyle(
                colors = PlanetCardColors(
                    containerGradient = listOf(
                        Color(0xFF06110A),
                        Color(0xFF0B1F12),
                        Color(0xFF12311B),
                    ),
                    contentColor = Color(0xFFEFFFF1),
                    glowColor = Color(0xFF7CFF8A).copy(alpha = 0.22f),
                    badgeContainerColor = Color(0xFF7CFF8A).copy(alpha = 0.14f),
                    badgeContentColor = Color(0xFFDFFFE3),
                ),
                minHeight = 196.dp,
                contentPadding = 20.dp,
                starCount = 22,
                showStars = true,
                showGlow = true,
            ),
            planetStyle = PlanetVisualStyle(
                colors = listOf(
                    Color(0xFFA8FF78),
                    Color(0xFF58D668),
                    Color(0xFF1E8E3E),
                ),
                glowColor = Color(0xFF90FF9A).copy(alpha = 0.24f),
                showRings = false,
                showAtmosphere = true,
                atmosphereColor = Color(0xFFBFFFC7).copy(alpha = 0.18f),
                showShadow = true,
                showCraters = false,
                showStripes = true,
                stripeColor = Color.White.copy(alpha = 0.10f),
            ),
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricPill(label = "Платформа", value = "Android")
                    MetricPill(label = "Город", value = "Краснодар")
                }
            },
            animationSpec = PlanetAnimationSpec(
                enableFloating = true,
                enableRotation = true,
                enableTwinklingStars = true,
                floatingAmplitude = 56.dp,
                rotationDurationMillis = 5800,
                floatingDurationMillis = 2200
            ),
        )
    }
}