package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.ui.CellMark
import dev.stekl0.mapmethod.core.ui.NotebookCell
import org.junit.Assert.assertEquals
import org.junit.Test

class NotebookCellsTest {
    private val palette = NotebookPalette.Light

    // Two rows: row 0 is the white band, row 1 the red band.
    private fun cell(orderIndex: Int, row: Int, filled: Boolean) =
        CellUi(orderIndex = orderIndex, row = row, col = orderIndex, filled = filled, isNext = false)

    @Test
    fun `filled Cells are graphite in every band while the Map is in progress`() {
        val cells =
            listOf(
                cell(0, row = 0, filled = true),
                cell(1, row = 1, filled = true),
                cell(2, row = 1, filled = false),
            )

        val drawn = notebookCells(cells, rows = 2, preview = emptySet(), isComplete = false, palette = palette)

        assertEquals(
            listOf(
                NotebookCell(row = 0, col = 0, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 1, col = 1, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 1, col = 2, mark = CellMark.Empty),
            ),
            drawn,
        )
    }

    @Test
    fun `a complete Map shows its Cells in the flag colours by band`() {
        val cells = listOf(cell(0, row = 0, filled = true), cell(1, row = 1, filled = true))

        val drawn = notebookCells(cells, rows = 2, preview = emptySet(), isComplete = true, palette = palette)

        assertEquals(
            listOf(
                NotebookCell(row = 0, col = 0, mark = CellMark.Filled(palette.flagWhite)),
                NotebookCell(row = 1, col = 1, mark = CellMark.Filled(palette.flagRed)),
            ),
            drawn,
        )
    }

    @Test
    fun `previewed Cells are outlined and the other empty Cells are tinted`() {
        val cells = listOf(cell(0, row = 0, filled = false), cell(1, row = 0, filled = false))

        val drawn = notebookCells(cells, rows = 2, preview = setOf(0), isComplete = false, palette = palette)

        assertEquals(listOf(CellMark.Preview, CellMark.Empty), drawn.map { it.mark })
    }
}
