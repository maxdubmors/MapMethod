package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.database.model.Cell
import dev.stekl0.mapmethod.core.database.repository.MapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

private class FakeMapRepository : MapRepository {
    val cells =
        MutableStateFlow(
            listOf(
                Cell(orderIndex = 0, row = 0, col = 0, filled = false),
                Cell(orderIndex = 1, row = 0, col = 1, filled = false),
            ),
        )

    override fun observeCells(): Flow<List<Cell>> = cells

    override suspend fun logPushUps(count: Int): Int {
        val targets =
            cells.value
                .asSequence()
                .filter { !it.filled }
                .sortedBy { it.orderIndex }
                .take(count.coerceAtLeast(0))
                .toList()
        cells.value =
            cells.value.map { cell ->
                if (cell.orderIndex in targets.map { it.orderIndex }) cell.copy(filled = true) else cell
            }
        return targets.size
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @Test
    fun `collecting cells exposes ordered state with next marked`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                expectInternalState {
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
    fun `logPushUps with one fills the next cell`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectInternalState {
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

    @Test
    fun `logPushUps with count fills that many next cells`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(2)
                expectInternalState {
                    copy(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = true, isNext = false),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = true, isNext = false),
                            ),
                        filledCount = 2,
                        totalCount = 2,
                    )
                }
                collecting.cancel()
            }
        }
}
