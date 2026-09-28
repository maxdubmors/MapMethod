package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.stekl0.mapmethod.core.screenshottesting.captureMultiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The shape of mainland France as the database seeds it, so the captures show the real Map.
private val FranceRows =
    listOf(
        ".......1......",
        ".......111....",
        "...1.111111...",
        "11111111111111",
        "1111111111111.",
        "..1111111111..",
        "..111111111...",
        "...111111111..",
        "...111111111..",
        "...1111111111.",
        "...111111111..",
        "....1111......",
        "......11......",
    )

private val FranceCellCount = FranceRows.sumOf { line -> line.count { it == '1' } }

/** France with its first [filledCount] Cells in fill order filled: row by row, west to east. */
private fun franceMap(filledCount: Int): MapUiState {
    val positions =
        FranceRows.flatMapIndexed { row, line ->
            line.indices.filter { line[it] == '1' }.map { col -> row to col }
        }
    val cells =
        positions.mapIndexed { orderIndex, (row, col) ->
            CellUi(
                orderIndex = orderIndex,
                row = row,
                col = col,
                filled = orderIndex < filledCount,
                isNext = orderIndex == filledCount,
            )
        }
    return MapUiState(cells = cells, filledCount = filledCount, totalCount = cells.size)
}

/**
 * The Map as its state alone draws it: no Log cascade or Completion plays, so every Cell is
 * settled and the capture needs no clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class MapScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty Map with the next Cell previewed`() {
        captureMap(name = "map_empty", state = franceMap(filledCount = 0))
    }

    @Test
    fun `partly filled Map in pencil`() {
        captureMap(name = "map_partly_filled", state = franceMap(filledCount = 40))
    }

    @Test
    fun `complete Map in the flag colours`() {
        captureMap(name = "map_complete", state = franceMap(filledCount = FranceCellCount))
    }

    private fun captureMap(name: String, state: MapUiState) {
        composeRule.captureMultiTheme(name = name) {
            MapScreen(
                state = state,
                cascade = rememberLogCascadeState(state),
                completion = rememberCompletionState(state),
                onLogCount = {},
            )
        }
    }
}
