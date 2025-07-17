package ru.redbyte.krdcompose.ui.games.chess.model

data class GameState(
    val board: Array<Array<Piece?>>,
    val currentTurn: PieceColor,
    val whiteCastleKingside: Boolean,
    val whiteCastleQueenside: Boolean,
    val blackCastleKingside: Boolean,
    val blackCastleQueenside: Boolean,
    val enPassantTarget: Pair<Int, Int>?,
    val selected: Pair<Int, Int>?,
    val moves: List<String>,
    val status: GameStatus
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (whiteCastleKingside != other.whiteCastleKingside) return false
        if (whiteCastleQueenside != other.whiteCastleQueenside) return false
        if (blackCastleKingside != other.blackCastleKingside) return false
        if (blackCastleQueenside != other.blackCastleQueenside) return false
        if (!board.contentDeepEquals(other.board)) return false
        if (currentTurn != other.currentTurn) return false
        if (enPassantTarget != other.enPassantTarget) return false
        if (selected != other.selected) return false
        if (moves != other.moves) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = whiteCastleKingside.hashCode()
        result = 31 * result + whiteCastleQueenside.hashCode()
        result = 31 * result + blackCastleKingside.hashCode()
        result = 31 * result + blackCastleQueenside.hashCode()
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + currentTurn.hashCode()
        result = 31 * result + (enPassantTarget?.hashCode() ?: 0)
        result = 31 * result + (selected?.hashCode() ?: 0)
        result = 31 * result + moves.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}