package ru.redbyte.krdcompose.games.chess.model

enum class GameStatus {
    RUNNING,
    CHECK_WHITE,
    CHECK_BLACK,
    MATE_WHITE_WINS,
    MATE_BLACK_WINS,
    STALEMATE
}