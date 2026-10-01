package dev.maxdubmors.mapmethod.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.maxdubmors.mapmethod.core.designsystem.theme.NotebookPalette
import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class MapCellsTest {
    private val palette = NotebookPalette.Light

    private val west = Color(0xFF112233)
    private val middle = Color(0xFF445566)
    private val east = Color(0xFF778899)

    // Three columns in fill order: column 0 is the western band, column 1 the middle band, column 2 the eastern band.
    private val threeBands =
        MapDefinition(
            id = MapId("three"),
            cells = List(3) { Cell(row = it, col = it) },
            flag = Flag(bands = listOf(west, middle, east).map { it.toArgb() }),
        )

    private fun marks(filledCount: Int, previewCount: Int = 0) =
        mapCells(MapWithProgress(threeBands, filledCount), palette, previewCount).map { it.mark }

    @Test
    fun `filled Cells are graphite in every band while the Map is in progress`() {
        assertEquals(
            listOf(
                NotebookCell(row = 0, col = 0, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 1, col = 1, mark = CellMark.Filled(palette.graphite)),
                NotebookCell(row = 2, col = 2, mark = CellMark.Empty),
            ),
            mapCells(MapWithProgress(threeBands, filledCount = 2), palette),
        )
    }

    @Test
    fun `a complete Map shows its Cells in its flag's colours by band`() {
        assertEquals(
            listOf(CellMark.Filled(west), CellMark.Filled(middle), CellMark.Filled(east)),
            marks(filledCount = 3),
        )
    }

    @Test
    fun `an empty Map shows only tinted Cells`() {
        assertEquals(listOf(CellMark.Empty, CellMark.Empty, CellMark.Empty), marks(filledCount = 0))
    }

    @Test
    fun `the next Cells are outlined and the other empty Cells are tinted`() {
        assertEquals(
            listOf(CellMark.Filled(palette.graphite), CellMark.Preview, CellMark.Empty),
            marks(filledCount = 1, previewCount = 1),
        )
    }

    @Test
    fun `without a preview no Cell is outlined, as in the Atlas`() {
        val drawn = (0..3).flatMap { marks(filledCount = it) }

        assertEquals(emptyList<CellMark>(), drawn.filter { it == CellMark.Preview })
    }

    @Test
    fun `a preview outlines no more Cells than are empty`() {
        assertEquals(
            listOf(CellMark.Filled(palette.graphite), CellMark.Preview, CellMark.Preview),
            marks(filledCount = 1, previewCount = 5),
        )
    }

    @Test
    fun `flag bands split by the Map's own columns`() {
        // Six columns over three bands: two columns each, the grid measured from the Cells themselves.
        val sixCols = threeBands.copy(cells = List(6) { Cell(row = 0, col = it) })

        assertEquals(
            listOf(west, west, middle, middle, east, east).map { CellMark.Filled(it) },
            mapCells(MapWithProgress(sixCols, filledCount = 6), palette).map { it.mark },
        )
    }
}
