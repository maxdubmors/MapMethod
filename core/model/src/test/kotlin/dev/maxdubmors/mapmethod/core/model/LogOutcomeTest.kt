package dev.maxdubmors.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogOutcomeTest {
    private val threeCells =
        MapDefinition(
            id = MapId("three"),
            cells =
                listOf(
                    Cell(orderIndex = 0, row = 0, col = 1),
                    Cell(orderIndex = 1, row = 1, col = 0),
                    Cell(orderIndex = 2, row = 1, col = 1),
                ),
            flag = Flag(bands = listOf(0xFF000000.toInt())),
        )

    private fun outcome(before: Int, after: Int) =
        LogOutcome(before = MapWithProgress(threeCells, before), after = MapWithProgress(threeCells, after))

    @Test
    fun `a Log that fills the last Cell brings Completion`() {
        val outcome = outcome(before = 1, after = 3)

        assertEquals(
            listOf(Cell(orderIndex = 1, row = 1, col = 0), Cell(orderIndex = 2, row = 1, col = 1)),
            outcome.filledCells,
        )
        assertTrue(outcome.completesMap)
    }

    @Test
    fun `a Log that leaves Cells empty fills its Cells without Completion`() {
        val outcome = outcome(before = 0, after = 1)

        assertEquals(listOf(Cell(orderIndex = 0, row = 0, col = 1)), outcome.filledCells)
        assertFalse(outcome.completesMap)
    }

    @Test
    fun `a Log on a complete Map fills nothing and brings no Completion again`() {
        val outcome = outcome(before = 3, after = 3)

        assertEquals(emptyList(), outcome.filledCells)
        assertFalse(outcome.completesMap)
    }
}
