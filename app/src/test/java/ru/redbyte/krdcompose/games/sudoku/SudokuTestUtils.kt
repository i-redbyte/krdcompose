package ru.redbyte.krdcompose.games.sudoku

internal object SudokuTestUtils {

    fun isValidCompleteSolution(sol: IntArray): Boolean {
        if (sol.size != 81) return false
        for (i in 0 until 81) if (sol[i] !in 1..9) return false
        // строки
        for (r in 0 until 9) {
            var mask = 0
            for (c in 0 until 9) mask = mask or (1 shl (sol[r * 9 + c] - 1))
            if (mask != 0x1FF) return false
        }
        // столбцы
        for (c in 0 until 9) {
            var mask = 0
            for (r in 0 until 9) mask = mask or (1 shl (sol[r * 9 + c] - 1))
            if (mask != 0x1FF) return false
        }
        // боксы 3x3
        for (br in 0 until 3) for (bc in 0 until 3) {
            var mask = 0
            for (dr in 0 until 3) for (dc in 0 until 3) {
                val r = br * 3 + dr
                val c = bc * 3 + dc
                mask = mask or (1 shl (sol[r * 9 + c] - 1))
            }
            if (mask != 0x1FF) return false
        }
        return true
    }

    /** Проверка, что заданные подсказки сами по себе не нарушают правила. */
    fun givensAreConsistent(puzzle: IntArray): Boolean {
        if (puzzle.size != 81) return false
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)
        for (i in 0 until 81) {
            val v = puzzle[i]
            if (v == 0) continue
            if (v !in 1..9) return false
            val r = i / 9
            val c = i % 9
            val b = (r / 3) * 3 + (c / 3)
            val bit = 1 shl (v - 1)
            if ((rowMask[r] and bit) != 0) return false
            if ((colMask[c] and bit) != 0) return false
            if ((boxMask[b] and bit) != 0) return false
            rowMask[r] = rowMask[r] or bit
            colMask[c] = colMask[c] or bit
            boxMask[b] = boxMask[b] or bit
        }
        return true
    }

    /**
     * Независимый солвер для тестов.
     * Возвращает:
     *  - пару (solution, true), если решение единственное;
     *  - пару (null, false), если решений 0 или >1 (не уникально).
     */
    fun solveIfUnique(puzzle: IntArray, limit: Int = 2): Pair<IntArray?, Boolean> {
        val board = puzzle.copyOf()
        val rowMask = IntArray(9)
        val colMask = IntArray(9)
        val boxMask = IntArray(9)

        // инициализация и проверка противоречий в подсказках
        for (i in 0 until 81) {
            val v = board[i]
            if (v != 0) {
                val r = i / 9
                val c = i % 9
                val b = (r / 3) * 3 + (c / 3)
                val bit = 1 shl (v - 1)
                if ((rowMask[r] and bit) != 0) return null to false
                if ((colMask[c] and bit) != 0) return null to false
                if ((boxMask[b] and bit) != 0) return null to false
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit
            }
        }

        var count = 0
        var firstSolution: IntArray? = null

        fun allowedMask(idx: Int): Int {
            val r = idx / 9
            val c = idx % 9
            val b = (r / 3) * 3 + (c / 3)
            val used = rowMask[r] or colMask[c] or boxMask[b]
            return 0x1FF and used.inv()
        }

        fun dfs(): Boolean { // true = пора останавливаться (достигли лимита)
            var bestIdx = -1
            var bestCount = 10
            for (i in 0 until 81) if (board[i] == 0) {
                val m = allowedMask(i)
                val bc = Integer.bitCount(m)
                if (bc == 0) return false
                if (bc < bestCount) {
                    bestCount = bc
                    bestIdx = i
                    if (bc == 1) break
                }
            }
            // нет пустых клеток — найдено решение
            if (bestIdx == -1) {
                count++
                if (count == 1) firstSolution = board.copyOf()
                return count >= limit
            }

            var m = allowedMask(bestIdx)
            val r = bestIdx / 9
            val c = bestIdx % 9
            val b = (r / 3) * 3 + (c / 3)
            while (m != 0) {
                val bit = m and -m
                val d = 1 + Integer.numberOfTrailingZeros(bit)
                m = m and (m - 1)

                board[bestIdx] = d
                rowMask[r] = rowMask[r] or bit
                colMask[c] = colMask[c] or bit
                boxMask[b] = boxMask[b] or bit

                if (dfs()) { // достигли лимита решений — останавливаемся
                    // откат перед выходом не обязателен, тесту всё равно
                    return true
                }

                // откат
                rowMask[r] = rowMask[r] and bit.inv()
                colMask[c] = colMask[c] and bit.inv()
                boxMask[b] = boxMask[b] and bit.inv()
                board[bestIdx] = 0
            }
            return false
        }

        dfs()
        return if (count == 1) firstSolution to true else null to false
    }
}