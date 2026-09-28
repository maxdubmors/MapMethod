package dev.stekl0.mapmethod.core.data.catalogue

import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.model.MapId
import org.junit.Assert.assertEquals
import org.junit.Test

class FranceTest {
    private val cells = France.cells

    @Test
    fun `France is identified as france`() {
        assertEquals(MapId("france"), France.id)
        assertEquals(France.id, FranceMapId)
    }

    @Test
    fun `France holds the method's hundred Cells`() {
        assertEquals(100, cells.size)
    }

    @Test
    fun `France is cropped to its own thirteen rows by fourteen columns`() {
        assertEquals(0, cells.minOf { it.row })
        assertEquals(12, cells.maxOf { it.row })
        assertEquals(0, cells.minOf { it.col })
        assertEquals(13, cells.maxOf { it.col })
    }

    @Test
    fun `France fills north to south, west to east`() {
        assertEquals(cells.indices.toList(), cells.map { it.orderIndex })
        assertEquals(cells.sortedWith(compareBy({ it.row }, { it.col })), cells)
        assertEquals(cells.size, cells.map { it.row to it.col }.toSet().size)
    }

    @Test
    fun `France starts at the tip of the north coast and ends at the Pyrenees`() {
        assertEquals(Cell(orderIndex = 0, row = 0, col = 7), cells.first())
        assertEquals(
            listOf(Cell(orderIndex = 98, row = 12, col = 6), Cell(orderIndex = 99, row = 12, col = 7)),
            cells.takeLast(2),
        )
    }

    @Test
    fun `France brings its blue, white and red, west to east`() {
        assertEquals(Flag(bands = listOf(0xFF0055A4.toInt(), 0xFFFFFFFF.toInt(), 0xFFEF4135.toInt())), France.flag)
    }

    @Test
    fun `France's fourteen columns split five blue, five white and four red`() {
        val (blue, white, red) = France.flag.bands

        assertEquals(
            List(5) { blue } + List(5) { white } + List(4) { red },
            (0 until 14).map { France.flag.colorAt(col = it, cols = 14) },
        )
    }

    @Test
    fun `the catalogue offers France`() {
        assertEquals(listOf(France), MapCatalogue.Default.maps)
        assertEquals(France, MapCatalogue.Default[FranceMapId])
    }
}
