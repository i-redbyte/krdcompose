package ru.redbyte.krdcompose.others.kepler.model

data class WorldConstraints(
    val scaleToFit: Boolean,
    val boundaryMode: BoundaryMode,
    val paddingFraction: Float,
    val maxWorldRadiusMultiplier: Float
)