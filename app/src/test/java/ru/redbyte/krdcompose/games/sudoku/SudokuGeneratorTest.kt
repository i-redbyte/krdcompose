package ru.redbyte.krdcompose.games.sudoku

import org.junit.Assert.*
import org.junit.Test
import ru.redbyte.krdcompose.games.sudoku.model.Difficulty
import kotlin.random.asKotlinRandom


class SudokuGeneratorImprovedTests {

    @Test
    fun solutionIsValid_complete() {
        val gen = SudokuGenerator()
        val p = gen.generate(Difficulty.NORMAL)
        assertTrue(SudokuTestUtils.isValidCompleteSolution(p.solution))
    }

    @Test
    fun givensDoNotBreakRules_andMatchSolution() {
        val gen = SudokuGenerator()
        val p = gen.generate(Difficulty.NORMAL)

        assertTrue("Givens are inconsistent", SudokuTestUtils.givensAreConsistent(p.givens))

        // все подсказки совпадают с соответствующими значениями решения
        for (i in 0 until 81) {
            if (p.givens[i] != 0) assertEquals(p.solution[i], p.givens[i])
        }
    }

    @Test
    fun uniqueSolution_forAllDifficulties_andMatchesProvidedSolution() {
        val gen = SudokuGenerator()
        for (d in listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)) {
            val p = gen.generate(d)
            val (solved, unique) = SudokuTestUtils.solveIfUnique(p.givens)
            assertTrue("Puzzle is not uniquely solvable for $d", unique)
            assertNotNull("Solver failed to produce solution for $d", solved)
            assertArrayEquals(
                "Solver solution differs from generator.solution for $d",
                p.solution,
                solved!!
            )
        }
    }

    @Test
    fun generateMany_hasCorrectCount_andEachUniqueSolvable() {
        val gen = SudokuGenerator()
        val list = gen.generateMany(Difficulty.NORMAL, 5)
        assertEquals(5, list.size)
        list.forEachIndexed { idx, p ->
            val (solved, unique) = SudokuTestUtils.solveIfUnique(p.givens)
            assertTrue("Puzzle #$idx not uniquely solvable", unique)
            assertArrayEquals("Puzzle #$idx solution mismatch", p.solution, solved!!)
        }
    }

    @Test
    fun deterministicWithSeed_reproducible() {
        val g1 = SudokuGenerator(java.util.Random(123456789L).asKotlinRandom())
        val g2 = SudokuGenerator(java.util.Random(123456789L).asKotlinRandom())
        val p1 = g1.generate(Difficulty.NORMAL)
        val p2 = g2.generate(Difficulty.NORMAL)
        assertArrayEquals(p1.givens, p2.givens)
        assertArrayEquals(p1.solution, p2.solution)
    }

    @Test
    fun brokenPuzzle_hasZeroSolutions() {
        // берём валидную раскладку и намеренно портим: две одинаковые цифры в одной строке
        val gen = SudokuGenerator()
        val p = gen.generate(Difficulty.EASY)
        val bad = p.givens.copyOf()
        // найдём первую строку где есть два непустых столбца и подменим одну цифру
        rowLoop@ for (r in 0 until 9) {
            val idxs = (0 until 9).map { r * 9 + it }.filter { bad[it] != 0 }
            if (idxs.size >= 2) {
                val d = bad[idxs[0]]
                bad[idxs[1]] = d // конфликт в строке
                break@rowLoop
            }
        }
        val (solved, unique) = SudokuTestUtils.solveIfUnique(bad)
        assertFalse(unique)
        assertNull(solved)
    }

    @Test
    fun difficultyOrdering_averageClues_easyMoreThanNormalMoreThanHard() {
        // Небольшая выборка для вероятностной проверки «сложность → меньше подсказок»
        val gen = SudokuGenerator()
        fun avgClues(d: Difficulty, n: Int) =
            (1..n).map { gen.generate(d).givens.count { it != 0 } }.average()

        val ae = avgClues(Difficulty.EASY, 6)
        val an = avgClues(Difficulty.NORMAL, 6)
        val ah = avgClues(Difficulty.HARD, 6)

        assertTrue("Expected EASY > NORMAL (got $ae <= $an)", ae > an)
        assertTrue("Expected NORMAL > HARD (got $an <= $ah)", an > ah)
    }
}
