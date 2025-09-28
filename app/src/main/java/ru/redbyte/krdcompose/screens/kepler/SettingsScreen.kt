package ru.redbyte.krdcompose.screens.kepler

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.redbyte.krdcompose.others.kepler.ColorRow
import ru.redbyte.krdcompose.others.kepler.DropdownEnum
import ru.redbyte.krdcompose.others.kepler.LabeledNumberField
import ru.redbyte.krdcompose.others.kepler.LabeledSlider
import ru.redbyte.krdcompose.others.kepler.model.ColorsConfig
import ru.redbyte.krdcompose.others.kepler.model.OrbitParams
import ru.redbyte.krdcompose.others.kepler.model.OrbitType
import ru.redbyte.krdcompose.others.kepler.model.VisualConfig
import ru.redbyte.krdcompose.others.kepler.model.WorldConstraints

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    orbitParams: OrbitParams,
    onOrbitChange: (OrbitParams) -> Unit,
    colors: ColorsConfig,
    onColorsChange: (ColorsConfig) -> Unit,
    visuals: VisualConfig,
    onVisualsChange: (VisualConfig) -> Unit,
    world: WorldConstraints,
    onWorldChange: (WorldConstraints) -> Unit,
    onStart: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kepler Settings") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Тип орбиты", fontWeight = FontWeight.SemiBold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                OrbitType.entries.forEach { t ->
                    FilterChip(
                        selected = orbitParams.type == t,
                        onClick = { onOrbitChange(orbitParams.copy(type = t)) },
                        label = { Text(t.name) }
                    )
                }
            }

            LabeledNumberField(
                label = "Начальная дистанция",
                value = orbitParams.initialDistance,
                onChange = { onOrbitChange(orbitParams.copy(initialDistance = it.coerceAtLeast(1.0))) }
            )

            if (orbitParams.type == OrbitType.Elliptical) {
                LabeledSlider(
                    label = "Эксцентриситет (0..0.99)",
                    value = orbitParams.eEllipse.toFloat(),
                    onChange = { onOrbitChange(orbitParams.copy(eEllipse = it.toDouble())) },
                    valueRange = 0f..0.99f,
                    steps = 0
                )
            }
            if (orbitParams.type == OrbitType.Hyperbolic) {
                LabeledSlider(
                    label = "Эксцентриситет (1.1..5.0)",
                    value = orbitParams.eHyper.toFloat(),
                    onChange = { onOrbitChange(orbitParams.copy(eHyper = it.toDouble())) },
                    valueRange = 1.1f..5f,
                    steps = 0
                )
            }

            Text("Пресеты", fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Circular, 100.0, 0.0, 1.5))
                }, label = { Text("LEO") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Elliptical, 120.0, 0.8, 1.5))
                }, label = { Text("Elliptic") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Parabolic, 100.0, 0.0, 1.5))
                }, label = { Text("Parabolic") })
                AssistChip(onClick = {
                    onOrbitChange(OrbitParams(OrbitType.Hyperbolic, 100.0, 0.0, 2.0))
                }, label = { Text("Hyperbolic") })
            }

            Text("Ограничения мира", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = world.scaleToFit,
                    onCheckedChange = { onWorldChange(world.copy(scaleToFit = it)) }
                )
                Text(
                    "Масштабировать, чтобы планета всегда была в кадре",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Поведение на границе:",
                    modifier = Modifier.weight(1f)
                )
                DropdownEnum(
                    current = world.boundaryMode,
                    onSelect = { onWorldChange(world.copy(boundaryMode = it)) }
                )
            }

            LabeledSlider(
                label = "Внутренние поля экрана (доля)",
                value = world.paddingFraction,
                onChange = { onWorldChange(world.copy(paddingFraction = it)) },
                valueRange = 0f..0.2f,
                steps = 0
            )

            LabeledSlider(
                label = "Макс. радиус мира = множитель × нач. дистанции",
                value = world.maxWorldRadiusMultiplier,
                onChange = { onWorldChange(world.copy(maxWorldRadiusMultiplier = it)) },
                valueRange = 2f..10f,
                steps = 8
            )

            Text("Цвета", fontWeight = FontWeight.SemiBold)
            ColorRow("Фон", colors.background) { onColorsChange(colors.copy(background = it)) }
            ColorRow("Звезда", colors.star) { onColorsChange(colors.copy(star = it)) }
            ColorRow("Планета", colors.planet) { onColorsChange(colors.copy(planet = it)) }
            ColorRow("Траектория", colors.trail) { onColorsChange(colors.copy(trail = it)) }
            ColorRow(
                "Вектор скорости",
                colors.velocity
            ) { onColorsChange(colors.copy(velocity = it)) }
            ColorRow("Вектор ускорения", colors.acceleration) {
                onColorsChange(
                    colors.copy(
                        acceleration = it
                    )
                )
            }

            Text("Визуализация", fontWeight = FontWeight.SemiBold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showTrail,
                    onCheckedChange = { onVisualsChange(visuals.copy(showTrail = it)) }
                )
                Text("Показывать траекторию", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showVelocity,
                    onCheckedChange = { onVisualsChange(visuals.copy(showVelocity = it)) }
                )
                Text("Показывать вектор скорости", modifier = Modifier.padding(start = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = visuals.showAcceleration,
                    onCheckedChange = { onVisualsChange(visuals.copy(showAcceleration = it)) }
                )
                Text("Показывать вектор ускорения", modifier = Modifier.padding(start = 8.dp))
            }

            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Старт") }
        }
    }
}
