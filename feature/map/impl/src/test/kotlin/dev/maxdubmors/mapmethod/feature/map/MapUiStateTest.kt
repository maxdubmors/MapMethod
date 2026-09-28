package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MapUiStateTest {
    private val twoCells =
        MapDefinition(
            id = MapId("two"),
            cells = listOf(Cell(orderIndex = 0, row = 0, col = 0), Cell(orderIndex = 1, row = 0, col = 1)),
            flag = Flag(bands = listOf(0xFF000000.toInt())),
        )

    @Test
    fun `a loaded Map has its empty Cells left to Log`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 1), isLoaded = true)

        assertEquals(1, state.remaining)
    }

    @Test
    fun `a Map not loaded yet has nothing to Log and is not complete`() {
        val state = MapUiState(map = MapWithProgress(twoCells, filledCount = 0), isLoaded = false)

        assertEquals(0, state.remaining)
        assertFalse(state.isComplete)
    }
}
