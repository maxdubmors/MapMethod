package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.ui.CellMark
import dev.stekl0.mapmethod.core.ui.NotebookCell
import org.junit.Assert.assertEquals
import org.junit.Test

class NotebookCellsTest {
    private val palette = NotebookPalette.Light

    private val west = Color(0xFF112233)
    private val middle = Color(0xFF445566)
    private val east = Color(0xFF778899)
    private val flag = Flag(bands = listOf(west, middle, east).map { it.toArgb() })

    // Three columns: column 0 is the western band, column 1 the middle band, column 2 the eastern band.
    private fun cell(orderIndex: Int, col: Int, filled: Boolean) =
        CellUi(orderIndex = orderIndex, row = orderIndex, col = col, filled = filled, isNext = false)

    private fun marks(cells: List<CellUi>, isComplete: Boolean, preview: Set<Int> = emptySet()) =
        notebookCells(cells, cols = 3, preview = preview, isComplete = isComplete, flag = flag, palette = palette)

    @Test
    fun `filled Cells are graphite in every band while the Map is in progress`() {
        val cells =
            listOf(
                cell(0, col = 0, filled = true),
                cell(1, col = 2, filled = true),
                cell(2, col = 2, filled = false),
            )

        val drawn = marks(cells, isComplete = false)

        assertEquals(
            listOf(
                NotebookCell(row = 0, col = 0, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 1, col = 2, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 2, col = 2, mark = CellMark.Empty),
            ),
            drawn,
        )
    }

    @Test
    fun `a complete Map shows its Cells in its flag's colours by band`() {
        val cells =
            listOf(
                cell(0, col = 0, filled = true),
                cell(1, col = 1, filled = true),
                cell(2, col = 2, filled = true),
            )

        val drawn = marks(cells, isComplete = true)

        assertEquals(
            listOf(
                NotebookCell(row = 0, col = 0, mark = CellMark.Filled(west)),
                NotebookCell(row = 1, col = 1, mark = CellMark.Filled(middle)),
                NotebookCell(row = 2, col = 2, mark = CellMark.Filled(east)),
            ),
            drawn,
        )
    }

    @Test
    fun `previewed Cells are outlined and the other empty Cells are tinted`() {
        val cells = listOf(cell(0, col = 0, filled = false), cell(1, col = 1, filled = false))

        val drawn = marks(cells, isComplete = false, preview = setOf(0))

        assertEquals(listOf(CellMark.Preview, CellMark.Empty), drawn.map { it.mark })
    }

    @Test
    fun `confetti comes in the Map's flag colours only`() {
        assertEquals(listOf(west, middle, east), confettiColors(flag))
    }
}
