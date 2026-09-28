package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.model.Cell
import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewCellsTest {
    private fun cells(count: Int): List<Cell> = List(count) { Cell(orderIndex = it, row = 0, col = it) }

    @Test
    fun `count one previews single next cell`() {
        assertEquals(setOf(0), previewOrderIndexes(cells(3), filledCount = 0, count = 1))
    }

    @Test
    fun `count previews that many next unfilled cells`() {
        assertEquals(setOf(0, 1, 2), previewOrderIndexes(cells(3), filledCount = 0, count = 3))
    }

    @Test
    fun `preview skips filled cells in fill order`() {
        assertEquals(setOf(1, 2), previewOrderIndexes(cells(3), filledCount = 1, count = 2))
    }

    @Test
    fun `null count falls back to single next cell`() {
        assertEquals(setOf(1), previewOrderIndexes(cells(3), filledCount = 1, count = null))
    }

    @Test
    fun `full map previews nothing`() {
        assertEquals(emptySet<Int>(), previewOrderIndexes(cells(2), filledCount = 2, count = 2))
        assertEquals(emptySet<Int>(), previewOrderIndexes(cells(2), filledCount = 2, count = null))
    }

    @Test
    fun `count beyond remaining previews only what is left`() {
        assertEquals(setOf(1, 2), previewOrderIndexes(cells(3), filledCount = 1, count = 99))
    }
}
