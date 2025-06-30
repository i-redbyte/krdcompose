package ru.redbyte.krdcompose.sapper.model

internal sealed interface GameState {
    object Idle : GameState
    object Running : GameState
    object Won : GameState
    object Lost : GameState
}
