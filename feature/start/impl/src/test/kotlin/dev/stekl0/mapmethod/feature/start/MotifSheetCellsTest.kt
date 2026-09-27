package dev.stekl0.mapmethod.feature.start

import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.ui.CellMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotifSheetCellsTest {
    private val palette = NotebookPalette.Light
    private val motif = ItalyMotif.cells()

    @Test
    fun `sheet shows the motif, framed by bare paper`() {
        val cells = motifSheetCells(showPreview = false, palette = palette)
        assertEquals(motif.map { (it.row + 1) to (it.col + 1) }, cells.map { it.row to it.col })
        assertEquals(ItalyMotif.rows.size + 2, MotifSheetRows)
        assertEquals(ItalyMotif.rows.first().length + 2, MotifSheetCols)
        assertTrue(cells.all { it.row in 1 until (MotifSheetRows - 1) && it.col in 1 until (MotifSheetCols - 1) })
    }

    @Test
    fun `motif's filled Cells are graphite`() {
        val cells = motifSheetCells(showPreview = false, palette = palette)
        assertEquals(
            motif.map { if (it.filled) CellMark.Filled(palette.graphite) else CellMark.Empty },
            cells.map { it.mark },
        )
    }

    @Test
    fun `only the next Cell in fill order is previewed`() {
        val cells = motifSheetCells(showPreview = true, palette = palette)
        val next = motif.indexOfFirst { !it.filled }
        assertEquals(listOf(next), cells.indices.filter { cells[it].mark == CellMark.Preview })
    }
}
