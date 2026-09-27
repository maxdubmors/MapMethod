package dev.stekl0.mapmethod.core.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class GridMetricsTest {
    @Test
    fun `cells are square and fit the tighter side of the canvas`() {
        assertEquals(
            GridMetrics(cell = 10f, width = 100f, height = 50f),
            gridMetrics(Size(100f, 200f), rows = 5, cols = 10),
        )
    }

    @Test
    fun `an empty grid has no geometry`() {
        assertEquals(GridMetrics(cell = 0f, width = 0f, height = 0f), gridMetrics(Size(100f, 200f), 0, 0))
    }

    @Test
    fun `grid is centred in the canvas`() {
        assertEquals(Offset(0f, 75f), gridMetrics(Size(100f, 200f), rows = 5, cols = 10).originIn(Size(100f, 200f)))
    }

    @Test
    fun `a Cell lies at its row and column from the grid's corner`() {
        val metrics = gridMetrics(Size(100f, 200f), rows = 5, cols = 10)
        assertEquals(Offset(30f, 95f), metrics.cellTopLeft(Size(100f, 200f), row = 2, col = 3))
    }

    @Test
    fun `grid lines run across the whole extent aligned with the Cells`() {
        assertEquals(listOf(5f, 15f, 25f, 35f, 45f), gridLinePositions(origin = 25f, cellSide = 10f, extent = 50f))
    }

    @Test
    fun `grid lines include both edges when the Cells meet them`() {
        assertEquals(listOf(0f, 10f, 20f), gridLinePositions(origin = 0f, cellSide = 10f, extent = 20f))
    }

    @Test
    fun `a zero-sized Cell draws no grid lines`() {
        assertEquals(emptyList<Float>(), gridLinePositions(origin = 0f, cellSide = 0f, extent = 20f))
    }
}
