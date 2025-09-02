package ru.redbyte.krdcompose.games.sudoku

import android.util.Log
import ru.redbyte.krdcompose.games.sudoku.model.Difficulty
import ru.redbyte.krdcompose.games.sudoku.model.SudokuPuzzle
import kotlin.math.min
import kotlin.random.Random

class SudokuGenerator(private val rng: Random = Random.Default) {
    private val fullMask = 0x1FF
    private val cellRow = IntArray(81) { it / 9 }
    private val cellCol = IntArray(81) { it % 9 }
    private val cellBox = IntArray(81) { ((it / 9) / 3) * 3 + ((it % 9) / 3) }
    private val allCells = IntArray(81) { it }

    fun generate(difficulty: Difficulty): SudokuPuzzle {
        val range = when (difficulty) {
            Difficulty.EASY -> 38..47
            Difficulty.NORMAL -> 30..37
            Difficulty.HARD -> 24..29
        }
        var bestPuzzle: IntArray? = null
        var bestClues = 81
        repeat(40) {
            val solution = IntArray(81)
            val rowMask = IntArray(9)
            val colMask = IntArray(9)
            val boxMask = IntArray(9)
            if (!fillSolution(solution, rowMask, colMask, boxMask)) return@repeat
            val puzzle = carvePuzzleToRange(solution, range.first, range.last)
            val clues = puzzle.count { it != 0 }
            if (clues in range) {
                return SudokuPuzzle(puzzle, solution, difficulty)
            }
            if (clues < bestClues && clues >= range.first) {
                bestPuzzle = puzzle
                bestClues = clues
            }
        }
        val fallbackSolution = IntArray(81)
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        if (!fillSolution(fallbackSolution, rowMask, colMask, boxMask)) throw IllegalStateException(
            "Failed to generate solution"
        )
        val fallbackPuzzle =
            bestPuzzle ?: carvePuzzleToRange(fallbackSolution, range.first, range.last)
        return SudokuPuzzle(fallbackPuzzle, fallbackSolution, difficulty)
    }

    fun generateMany(difficulty: Difficulty, count: Int): List<SudokuPuzzle> {
        val list = ArrayList<SudokuPuzzle>(count)
        repeat(count) { list += generate(difficulty) }
        return list
    }

    private fun fillSolution(
        board: IntArray,
        rowMask: IntArray,
        colMask: IntArray,
        boxMask: IntArray
    ): Boolean {
        var bestIdx = -1
        var bestCount = 10
        var foundEmpty = false
        for (i in 0 until 81) {
            if (board[i] == 0) {
                foundEmpty = true
                val mask = allowedMask(i, rowMask, colMask, boxMask)
                val cnt = Integer.bitCount(mask)
                if (cnt == 0) return false
                if (cnt < bestCount) {
                    bestCount = cnt
                    bestIdx = i
                    if (cnt == 1) break
                }
            }
        }
        if (!foundEmpty) return true
        val idx = bestIdx
        val mask = allowedMask(idx, rowMask, colMask, boxMask)
        val order = bitsToShuffledDigits(mask)
        val r = cellRow[idx]
        val c = cellCol[idx]
        val b = cellBox[idx]
        for (d in order) {
            val bit = 1 shl (d - 1)
            board[idx] = d
            rowMask[r] = rowMask[r] or bit
            colMask[c] = colMask[c] or bit
            boxMask[b] = boxMask[b] or bit
            if (fillSolution(board, rowMask, colMask, boxMask)) return true
            rowMask[r] = rowMask[r] and bit.inv()
            colMask[c] = colMask[c] and bit.inv()
            boxMask[b] = boxMask[b] and bit.inv()
            board[idx] = 0
        }
        return false
    }

    private fun carvePuzzleToRange(solution: IntArray, lowerBound: Int, upperBound: Int): IntArray {
        val puzzle = solution.copyOf()
        var clues = 81
        shuffle(allCells)
        for (pos in allCells) {
            if (clues <= upperBound) break
            if (puzzle[pos] == 0) continue
            val sym = 80 - pos
            val a = puzzle[pos]
            val b = if (sym != pos) puzzle[sym] else 0
            val canRemovePair = (sym != pos && b != 0)
            if (canRemovePair && clues - 2 >= lowerBound) {
                puzzle[pos] = 0
                puzzle[sym] = 0
                if (hasUniqueSolution(puzzle)) {
                    clues -= 2
                    continue
                } else {
                    puzzle[pos] = a
                    puzzle[sym] = b
                }
            }
            if (clues - 1 >= lowerBound) {
                puzzle[pos] = 0
                if (hasUniqueSolution(puzzle)) {
                    clues -= 1
                } else {
                    puzzle[pos] = a
                    if (canRemovePair && clues - 1 >= lowerBound) {
                        puzzle[sym] = 0
                        if (hasUniqueSolution(puzzle)) {
                            clues -= 1
                        } else {
                            puzzle[sym] = b
                        }
                    }
                }
            }
        }
        var passes = 0
        val tighten = min(12, 81 - lowerBound)
        while (clues > upperBound && passes < tighten) {
            var progress = false
            shuffle(allCells)
            for (pos in allCells) {
                if (clues <= upperBound) break
                if (puzzle[pos] == 0) continue
                val sym = 80 - pos
                val a = puzzle[pos]
                val b = if (sym != pos) puzzle[sym] else 0
                val canRemovePair = (sym != pos && b != 0)
                if (canRemovePair && clues - 2 >= lowerBound) {
                    puzzle[pos] = 0
                    puzzle[sym] = 0
                    if (hasUniqueSolution(puzzle)) {
                        clues -= 2
                        progress = true
                        continue
                    } else {
                        puzzle[pos] = a
                        puzzle[sym] = b
                    }
                }
                if (clues - 1 >= lowerBound) {
                    puzzle[pos] = 0
                    if (hasUniqueSolution(puzzle)) {
                        clues -= 1
                        progress = true
                    } else {
                        puzzle[pos] = a
                        if (canRemovePair && clues - 1 >= lowerBound) {
                            puzzle[sym] = 0
                            if (hasUniqueSolution(puzzle)) {
                                clues -= 1
                                progress = true
                            } else {
                                puzzle[sym] = b
                            }
                        }
                    }
                }
            }
            if (!progress) break
            passes++
        }
        return puzzle
    }

    private fun hasUniqueSolution(puzzle: IntArray): Boolean {
        val board = puzzle.copyOf()
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        for (i in 0 until 81) {
            val v = board[i]
            if (v != 0) {
                val bit = 1 shl (v - 1)
                val r = cellRow[i]
                val c = cellCol[i]
                val b = cellBox[i]
                if ((rowMask[r] and bit) != 0) return false
                if ((colMask[c] and bit) != 0) return false
                if ((boxMask[b] and bit) != 0) return false
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit
            }
        }
        var count = 0
        fun dfs(): Boolean {
            var bestIdx = -1
            var bestCount = 10
            for (i in 0 until 81) if (board[i] == 0) {
                val m = allowedMask(i, rowMask, colMask, boxMask)
                val bc = Integer.bitCount(m)
                if (bc == 0) return false
                if (bc < bestCount) {
                    bestCount = bc
                    bestIdx = i
                    if (bc == 1) break
                }
            }
            if (bestIdx == -1) {
                count++
                return count >= 2
            }
            var m = allowedMask(bestIdx, rowMask, colMask, boxMask)
            val r = cellRow[bestIdx]
            val c = cellCol[bestIdx]
            val b = cellBox[bestIdx]
            while (m != 0) {
                val bit = m and -m
                val d = 1 + Integer.numberOfTrailingZeros(bit)
                m = m and (m - 1)
                board[bestIdx] = d
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit
                if (dfs()) {
                    board[bestIdx] = 0
                    rowMask[r] = rowMask[r] and bit.inv()
                    colMask[c] = colMask[c] and bit.inv()
                    boxMask[b] = boxMask[b] and bit.inv()
                    return true
                }
                rowMask[r] = rowMask[r] and bit.inv()
                colMask[c] = colMask[c] and bit.inv()
                boxMask[b] = boxMask[b] and bit.inv()
                board[bestIdx] = 0
                if (count >= 2) return true
            }
            return false
        }
        dfs()
        return count == 1
    }

    private fun allowedMask(
        idx: Int,
        rowMask: IntArray,
        colMask: IntArray,
        boxMask: IntArray
    ): Int {
        val used = rowMask[cellRow[idx]] or colMask[cellCol[idx]] or boxMask[cellBox[idx]]
        return fullMask and used.inv()
    }

    private fun bitsToShuffledDigits(mask: Int): IntArray {
        val arr = IntArray(Integer.bitCount(mask))
        var m = mask
        var i = 0
        while (m != 0) {
            val bit = m and -m
            val d = 1 + Integer.numberOfTrailingZeros(bit)
            arr[i++] = d
            m = m and (m - 1)
        }
        for (j in arr.lastIndex downTo 1) {
            val k = rng.nextInt(j + 1)
            val t = arr[j]
            arr[j] = arr[k]
            arr[k] = t
        }
        return arr
    }

    private fun shuffle(a: IntArray) {
        for (i in a.lastIndex downTo 1) {
            val j = rng.nextInt(i + 1)
            val t = a[i]
            a[i] = a[j]
            a[j] = t
        }
    }

    fun logBoard(board: IntArray) {
        Log.d("_debug", board.toBoardString())
    }

    private fun IntArray.toBoardString(): String {
        val sb = StringBuilder(9 * 10)
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val v = this[r * 9 + c]
                sb.append(if (v == 0) '.' else ('0'.code + v).toChar())
                if (c != 8) sb.append(' ')
            }
            if (r != 8) sb.append('\n')
        }
        return sb.toString()
    }
}
