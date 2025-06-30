package ru.redbyte.krdcompose.sapper

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

internal interface GameCommand {
    fun execute()
}

internal class RevealCellCommand(private val vm: SapperViewModel, private val cell: Cell) : GameCommand {
    override fun execute() = vm.revealCell(cell)
}

internal class FlagCellCommand(private val vm: SapperViewModel, private val cell: Cell) : GameCommand {
    override fun execute() = vm.toggleFlag(cell)
}

internal sealed interface GameState {
    object Idle : GameState
    object Running : GameState
    object Won : GameState
    object Lost : GameState
}
