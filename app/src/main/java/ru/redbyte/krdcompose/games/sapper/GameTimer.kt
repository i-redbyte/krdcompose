package ru.redbyte.krdcompose.games.sapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

