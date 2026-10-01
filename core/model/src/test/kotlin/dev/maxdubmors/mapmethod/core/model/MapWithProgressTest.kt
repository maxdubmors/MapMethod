package dev.maxdubmors.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapWithProgressTest {
    private val threeCells =
        MapDefinition(
            id = MapId("three"),
            cells =
                listOf(
                    Cell(row = 0, col = 1),
                    Cell(row = 1, col = 0),
                    Cell(row = 1, col = 1),
                ),
            flag = Flag(bands = listOf(0xFF000000.toInt())),
        )

    @Test
    fun `a fresh Map has every Cell empty`() {
        val map = MapWithProgress(threeCells, filledCount = 0)

        assertEquals(emptyList<Cell>(), map.filledCells)
        assertEquals(listOf(Cell(row = 0, col = 1), Cell(row = 1, col = 0), Cell(row = 1, col = 1)), map.emptyCells)
        assertEquals(3, map.emptyCount)
        assertFalse(map.isComplete)
    }

    @Test
    fun `a partial Map's next Cells follow its filled ones in fill order`() {
        val map = MapWithProgress(threeCells, filledCount = 1)

        assertEquals(listOf(Cell(row = 0, col = 1)), map.filledCells)
        assertEquals(listOf(Cell(row = 1, col = 0)), map.nextCells(1))
        assertEquals(listOf(Cell(row = 1, col = 0), Cell(row = 1, col = 1)), map.nextCells(2))
        assertEquals(2, map.emptyCount)
        assertFalse(map.isComplete)
    }

    @Test
    fun `a Map previews no more next Cells than it has empty`() {
        val map = MapWithProgress(threeCells, filledCount = 1)

        assertEquals(listOf(Cell(row = 1, col = 0), Cell(row = 1, col = 1)), map.nextCells(99))
    }

    @Test
    fun `a Map with every Cell filled is complete and previews nothing`() {
        val map = MapWithProgress(threeCells, filledCount = 3)

        assertEquals(threeCells.cells, map.filledCells)
        assertEquals(0, map.emptyCount)
        assertEquals(emptyList<Cell>(), map.nextCells(1))
        assertTrue(map.isComplete)
    }

    @Test
    fun `a Map filled beyond its Cells is complete with none empty`() {
        // Progress stored before the Map's mask lost Cells.
        val map = MapWithProgress(threeCells, filledCount = 5)

        assertEquals(threeCells.cells, map.filledCells)
        assertEquals(0, map.emptyCount)
        assertTrue(map.isComplete)
    }

    @Test
    fun `a Map's filled count is never negative`() {
        assertFailsWith<IllegalArgumentException> { MapWithProgress(threeCells, filledCount = -1) }
    }
}
