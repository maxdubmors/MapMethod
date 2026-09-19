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

    override suspend fun logPushUps(count: Int): Int {
        val targets =
            cells.value
                .filter { !it.filled }
                .sortedBy { it.orderIndex }
                .take(count.coerceAtLeast(0))
        cells.value =
            cells.value.map { cell ->
                if (cell.orderIndex in targets.map { it.orderIndex }) cell.copy(filled = true) else cell
            }
        return targets.size
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
    public fun `logPushUps with one fills the next cell`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.test(this, MapUiState.EMPTY) {
                expectInitialState()
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
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

    @Test
    public fun `logPushUps with count fills that many next cells`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.test(this, MapUiState.EMPTY) {
                expectInitialState()
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(2)
                expectState {
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
