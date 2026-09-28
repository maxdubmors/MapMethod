package dev.maxdubmors.mapmethod.feature.start

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItalyMotifTest {
    private val cells = ItalyMotif.cells()

    @Test
    fun `Cells follow fill order row by row north to south, west to east`() {
        assertEquals(cells.sortedWith(compareBy({ it.row }, { it.col })), cells)
    }

    @Test
    fun `every mask mark becomes one Cell`() {
        assertEquals(ItalyMotif.rows.sumOf { line -> line.count { it == '1' } }, cells.size)
    }

    @Test
    fun `first filled count Cells in fill order are filled`() {
        assertEquals(List(cells.size) { it < ItalyMotif.FILLED_COUNT }, cells.map { it.filled })
    }

    @Test
    fun `north is filled while the south and Sicily stay empty`() {
        val lastRow = ItalyMotif.rows.lastIndex
        assertTrue(cells.filter { it.row == 0 }.all { it.filled })
        assertTrue(cells.filter { it.row >= lastRow / 2 }.none { it.filled })
    }

    @Test
    fun `filling stops midway through a row so the next Cell is visible`() {
        val next = cells.first { !it.filled }
        assertTrue(cells.any { it.filled && it.row == next.row })
    }
}
