package ru.redbyte.krdcompose.games.sapper.model

internal sealed interface GameState {
    object Idle : GameState
    object Running : GameState
    object Won : GameState
    object Lost : GameState
}
