package ru.redbyte.krdcompose.sapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.redbyte.krdcompose.sapper.model.Cell

internal class GameTimer(private val scope: CoroutineScope) {
    private var job: Job? = null

    fun start(onTick: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            while (true) {
                delay(1_000)
                onTick()
            }
        }
    }

    fun stop() = job?.cancel()
}

