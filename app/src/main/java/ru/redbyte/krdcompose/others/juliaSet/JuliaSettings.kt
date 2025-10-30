package ru.redbyte.krdcompose.others.juliaSet

data class JuliaSettings(
    val useAgsl: Boolean,
    val cRe: Float,
    val cIm: Float,
    val zoom: Float,
    val centerX: Float,
    val centerY: Float,
    val iterations: Int,
    val escape: Float,
    val animateC: Boolean,
    val animateZoom: Boolean,
    val animateCenter: Boolean,
    val paletteShift: Float,
    val paletteScale: Float,
    val highRes: Boolean
)