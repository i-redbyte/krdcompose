package ru.redbyte.krdcompose.screens.kepler

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import ru.redbyte.krdcompose.others.kepler.SettingsScreen
import ru.redbyte.krdcompose.others.kepler.SimulationScreen
import ru.redbyte.krdcompose.others.kepler.model.BoundaryMode
import ru.redbyte.krdcompose.others.kepler.model.ColorsConfig
import ru.redbyte.krdcompose.others.kepler.model.OrbitParams
import ru.redbyte.krdcompose.others.kepler.model.OrbitType
import ru.redbyte.krdcompose.others.kepler.model.VisualConfig
import ru.redbyte.krdcompose.others.kepler.model.WorldConstraints

@Composable
fun KeplerScreen() {
    var started by rememberSaveable { mutableStateOf(false) }

    val orbitSaver = listSaver<OrbitParams, Any>(
        save = { listOf(it.type.name, it.initialDistance, it.eEllipse, it.eHyper) },
        restore = { list ->
            OrbitParams(
                type = OrbitType.valueOf(list[0] as String),
                initialDistance = (list[1] as Number).toDouble(),
                eEllipse = (list[2] as Number).toDouble(),
                eHyper = (list[3] as Number).toDouble()
            )
        }
    )
    var orbitParams by rememberSaveable(stateSaver = orbitSaver) {
        mutableStateOf(OrbitParams(OrbitType.Circular, 100.0, 0.5, 1.5))
    }

    val colorsSaver = listSaver<ColorsConfig, Any>(
        save = {
            listOf(
                it.background.value.toLong(),
                it.star.value.toLong(),
                it.planet.value.toLong(),
                it.trail.value.toLong(),
                it.velocity.value.toLong(),
                it.acceleration.value.toLong()
            )
        },
        restore = { list ->
            ColorsConfig(
                Color(list[0] as Long),
                Color(list[1] as Long),
                Color(list[2] as Long),
                Color(list[3] as Long),
                Color(list[4] as Long),
                Color(list[5] as Long)
            )
        }
    )
    var colors by rememberSaveable(stateSaver = colorsSaver) {
        mutableStateOf(
            ColorsConfig(
                background = Color(0xFF0B0E16),
                star = Color(0xFFFFD54F),
                planet = Color(0xFF4FC3F7),
                trail = Color(0x80FFFFFF),
                velocity = Color(0xFF66BB6A),
                acceleration = Color(0xFFEF5350)
            )
        )
    }

    val visualsSaver = listSaver<VisualConfig, Any>(
        save = { listOf(it.showTrail, it.showVelocity, it.showAcceleration) },
        restore = { list ->
            VisualConfig(
                list[0] as Boolean,
                list[1] as Boolean,
                list[2] as Boolean
            )
        }
    )
    var visuals by rememberSaveable(stateSaver = visualsSaver) {
        mutableStateOf(
            VisualConfig(
                showTrail = true,
                showVelocity = false,
                showAcceleration = false
            )
        )
    }

    val worldSaver = listSaver<WorldConstraints, Any>(
        save = {
            listOf(
                it.scaleToFit,
                it.boundaryMode.name,
                it.paddingFraction,
                it.maxWorldRadiusMultiplier
            )
        },
        restore = { list ->
            WorldConstraints(
                scaleToFit = list[0] as Boolean,
                boundaryMode = BoundaryMode.valueOf(list[1] as String),
                paddingFraction = (list[2] as Number).toFloat(),
                maxWorldRadiusMultiplier = (list[3] as Number).toFloat()
            )
        }
    )
    var world by rememberSaveable(stateSaver = worldSaver) {
        mutableStateOf(
            WorldConstraints(
                scaleToFit = true,
                boundaryMode = BoundaryMode.Bounce,
                paddingFraction = 0.08f,
                maxWorldRadiusMultiplier = 4f
            )
        )
    }

    if (!started) {
        SettingsScreen(
            orbitParams = orbitParams,
            onOrbitChange = { orbitParams = it },
            colors = colors,
            onColorsChange = { colors = it },
            visuals = visuals,
            onVisualsChange = { visuals = it },
            world = world,
            onWorldChange = { world = it },
            onStart = { started = true }
        )
    } else {
        SimulationScreen(
            orbitParams = orbitParams,
            colors = colors,
            visuals = visuals,
            world = world,
            onBack = { started = false },
            onPreset = { p -> orbitParams = p }
        )
    }
}