package dev.stekl0.mapmethod.feature.start

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItalyMotifTest {
    private val cells = ItalyMotif.cells()

    @Test
    fun `cells follow fill order row by row north to south, west to east`() {
        assertEquals(cells.indices.toList(), cells.map { it.orderIndex })
        assertEquals(cells.sortedWith(compareBy({ it.row }, { it.col })), cells)
    }

    @Test
    fun `every mask mark becomes one cell`() {
        assertEquals(ItalyMotif.rows.sumOf { line -> line.count { it == '1' } }, cells.size)
    }

    @Test
    fun `first shaded count cells in fill order are shaded`() {
        assertEquals(List(cells.size) { it < ItalyMotif.SHADED_COUNT }, cells.map { it.shaded })
    }

    @Test
    fun `north is shaded while the south and Sicily stay empty`() {
        val lastRow = ItalyMotif.rows.lastIndex
        assertTrue(cells.filter { it.row == 0 }.all { it.shaded })
        assertTrue(cells.filter { it.row >= lastRow / 2 }.none { it.shaded })
    }

    @Test
    fun `shading stops midway through a row so the next Cell is visible`() {
        val next = cells.first { !it.shaded }
        assertTrue(cells.any { it.shaded && it.row == next.row })
    }
}
