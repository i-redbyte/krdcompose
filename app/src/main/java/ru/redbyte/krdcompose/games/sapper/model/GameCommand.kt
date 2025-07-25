package ru.redbyte.krdcompose.games.sapper.model

import ru.redbyte.krdcompose.games.sapper.SapperViewModel

internal interface GameCommand {
    fun execute()
}

internal class RevealCellCommand(private val vm: SapperViewModel, private val cell: Cell) : GameCommand {
    override fun execute() = vm.revealCell(cell)
}

internal class FlagCellCommand(private val vm: SapperViewModel, private val cell: Cell) : GameCommand {
    override fun execute() = vm.toggleFlag(cell)
}
