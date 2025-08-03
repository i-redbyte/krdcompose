package ru.redbyte.krdcompose.games.hanoiTowers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class HanoiGame(rods: Int, val rings: Int) {
    val towers: List<SnapshotStateList<Int>> = List(rods) { mutableStateListOf() }
    var moves by mutableIntStateOf(0)
        private set

    init {
        for (r in rings downTo 1) towers[0].add(r)
    }

    fun canMove(from: Int, to: Int): Boolean {
        if (towers[from].isEmpty()) return false
        val ring = towers[from].last()
        return towers[to].isEmpty() || ring < towers[to].last()
    }

    fun popRing(from: Int): Int = towers[from].removeAt(towers[from].lastIndex)

    fun pushRing(to: Int, ring: Int) {
        towers[to].add(ring); moves++
    }

    fun isVictory(): Boolean = towers.last().size == rings
}