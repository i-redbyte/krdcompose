package ru.redbyte.krdcompose.games.sudoku

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.redbyte.krdcompose.games.sudoku.model.Difficulty

@RunWith(Parameterized::class)
class SudokuClueRangeTest(
    private val difficulty: Difficulty,
    private val range: IntRange
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} → clues in {1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(Difficulty.EASY, 38..47),
            arrayOf(Difficulty.NORMAL, 30..37),
            arrayOf(Difficulty.HARD, 24..29),
        )
    }

    @Test
    fun cluesAreWithinRange_multipleSamples() {
        val gen = SudokuGenerator()
        repeat(5) {
            val p = gen.generate(difficulty)
            val clues = p.givens.count { it != 0 }
            assertTrue("Clues=$clues not in $range for $difficulty", clues in range)
        }
    }
}
