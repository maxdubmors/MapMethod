package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.database.model.Cell
import dev.stekl0.mapmethod.core.database.repository.MapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.test.test

private class FakeMapRepository : MapRepository {
    val cells =
        MutableStateFlow(
            listOf(
                Cell(orderIndex = 0, row = 0, col = 0, filled = false),
                Cell(orderIndex = 1, row = 0, col = 1, filled = false),
            ),
        )

    override fun observeCells(): Flow<List<Cell>> = cells

    override suspend fun logPushUps(): Boolean {
        val next = cells.value.firstOrNull { !it.filled } ?: return false
        cells.value = cells.value.map { if (it.orderIndex == next.orderIndex) it.copy(filled = true) else it }
        return true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
public class MapViewModelTest {
    @Test
    public fun `collecting cells exposes ordered state with next marked`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.test(this, MapUiState.EMPTY) {
                expectInitialState()
                val collecting = runOnCreate()
                expectState {
                    copy(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = false, isNext = true),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = false),
                            ),
                        filledCount = 0,
                        totalCount = 2,
                    )
                }
                collecting.cancel()
            }
        }

    @Test
    public fun `logPushUps fills the next cell`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.test(this, MapUiState.EMPTY) {
                expectInitialState()
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps()
                expectState {
                    copy(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = true, isNext = false),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = true),
                            ),
                        filledCount = 1,
                        totalCount = 2,
                    )
                }
                collecting.cancel()
            }
        }
}
