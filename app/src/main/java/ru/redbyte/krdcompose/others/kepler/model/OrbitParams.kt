package ru.redbyte.krdcompose.others.kepler.model

data class OrbitParams(
    val type: OrbitType,
    val initialDistance: Double,
    val eEllipse: Double,
    val eHyper: Double
)