package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.database.model.Cell
import dev.stekl0.mapmethod.core.database.repository.MapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

private fun twoCells(firstFilled: Boolean = false, secondFilled: Boolean = false) =
    listOf(
        Cell(orderIndex = 0, row = 0, col = 0, filled = firstFilled),
        Cell(orderIndex = 1, row = 0, col = 1, filled = secondFilled),
    )

private class FakeMapRepository(
    initial: List<Cell> = twoCells(),
) : MapRepository {
    val cells = MutableStateFlow(initial)

    override fun observeCells(): Flow<List<Cell>> = cells

    override suspend fun logPushUps(count: Int): List<Int> {
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
        return targets.map { it.orderIndex }
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
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(0)))
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
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(0, 1)))
                expectSideEffect(MapEvent.Completion)
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

    @Test
    fun `a Log on a partial Map emits exactly the Cells it filled`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository(twoCells(true, false)))

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(5)
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(1)))
                expectSideEffect(MapEvent.Completion)
                skipItems(1)
                collecting.cancel()
            }
        }

    @Test
    fun `a Log that fills the last Cell emits Completion once, after its Cells`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository(twoCells(true, false)))

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(1)))
                expectSideEffect(MapEvent.Completion)
                skipItems(1)
                viewModel.logPushUps(1)
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `a Log that leaves Cells empty emits no Completion`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository())

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(0)))
                skipItems(1)
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `a Log on a complete Map emits nothing`() =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository(twoCells(true, true)))

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `loading an empty Map emits nothing`() = assertLoadingEmitsNothing(twoCells(false, false))

    @Test
    fun `loading a partial Map emits nothing`() = assertLoadingEmitsNothing(twoCells(true, false))

    @Test
    fun `loading a complete Map emits nothing`() = assertLoadingEmitsNothing(twoCells(true, true))

    private fun assertLoadingEmitsNothing(cells: List<Cell>) =
        runTest {
            val viewModel = MapViewModel(FakeMapRepository(cells))

            viewModel.testWithInternalState(this, MapUiState.EMPTY) {
                val collecting = runOnCreate()
                skipItems(1)
                expectNoItems()
                collecting.cancel()
            }
        }
}
