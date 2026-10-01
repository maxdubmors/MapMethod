package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapUiStateTest {
    private val twoCells =
        MapDefinition(
            id = MapId("two"),
            cells = listOf(Cell(row = 0, col = 0), Cell(row = 0, col = 1)),
            flag = Flag(bands = listOf(0xFF000000.toInt())),
        )

    @Test
    fun `a loaded Map has its empty Cells to Log`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 1), isLoaded = true)

        assertEquals(1, state.emptyCount)
    }

    @Test
    fun `a Map not loaded yet has nothing to Log and is not complete`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 0), isLoaded = false)

        assertEquals(0, state.emptyCount)
        assertFalse(state.isComplete)
    }

    @Test
    fun `a loaded Map with every Cell filled is complete`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 2), isLoaded = true)

        assertTrue(state.isComplete)
    }

    @Test
    fun `a loaded Map previews as many Cells as the Log entry`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 0), isLoaded = true)

        assertEquals(2, state.previewCount(entry = 2))
    }

    @Test
    fun `an empty or invalid Log entry previews the single next Cell`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 0), isLoaded = true)

        assertEquals(1, state.previewCount(entry = null))
    }

    @Test
    fun `a Map not loaded yet previews no Cell`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 0), isLoaded = false)

        assertEquals(0, state.previewCount(entry = 2))
        assertEquals(0, state.previewCount(entry = null))
    }
}
