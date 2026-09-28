package dev.maxdubmors.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapWithProgressTest {
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

    @Test
    fun `a fresh Map has every Cell left and its first Cell next`() {
        val map = MapWithProgress(threeCells, filledCount = 0)

        assertEquals(3, map.totalCount)
        assertEquals(Cell(orderIndex = 0, row = 0, col = 1), map.nextCell)
        assertFalse(map.isComplete)
    }

    @Test
    fun `a partial Map's next Cell follows its filled ones in fill order`() {
        val map = MapWithProgress(threeCells, filledCount = 2)

        assertEquals(Cell(orderIndex = 2, row = 1, col = 1), map.nextCell)
        assertFalse(map.isComplete)
    }

    @Test
    fun `a Map with every Cell filled is complete and has no next Cell`() {
        val map = MapWithProgress(threeCells, filledCount = 3)

        assertNull(map.nextCell)
        assertTrue(map.isComplete)
    }
}
