package ru.redbyte.krdcompose.others.juliaSet

import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class JuliaViewModel : ViewModel() {
    private val _settings = MutableStateFlow(
        JuliaSettings(
            useAgsl = Build.VERSION.SDK_INT >= 33,
            cRe = -0.8f,
            cIm = 0.156f,
            zoom = 1.0f,
            centerX = 0f,
            centerY = 0f,
            iterations = 300,
            escape = 4f,
            animateC = true,
            animateZoom = false,
            animateCenter = false,
            paletteShift = 0.0f,
            paletteScale = 1.0f,
            highRes = false
        )
    )
    val settings = _settings.asStateFlow()
    fun update(block: (JuliaSettings) -> JuliaSettings) {
        _settings.value = block(_settings.value)
    }
}
