package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MapUiStateTest {
    private val twoCells =
        listOf(
            CellUi(orderIndex = 0, row = 0, col = 0, filled = true, isNext = false),
            CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = true),
        )

    @Test
    fun `a loaded Map has its empty Cells left to Log`() {
        val state = MapUiState(cells = twoCells, filledCount = 1, totalCount = 2, isLoaded = true)

        assertEquals(1, state.remaining)
    }

    @Test
    fun `a Map not loaded yet has nothing to Log and is not complete`() {
        val state = MapUiState(cells = twoCells, filledCount = 0, totalCount = 2, isLoaded = false)

        assertEquals(0, state.remaining)
        assertFalse(state.isComplete)
    }
}
