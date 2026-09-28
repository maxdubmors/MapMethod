package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.data.repository.MapRepository
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

private val TwoCells =
    MapDefinition(
        id = MapId("two"),
        cells = listOf(Cell(orderIndex = 0, row = 0, col = 0), Cell(orderIndex = 1, row = 0, col = 1)),
    )

/**
 * Every identity observes one Map of [TwoCells] whose first [initialFilledCount] Cells are filled;
 * its progress is at hand without waiting once [loaded].
 */
private class FakeMapRepository(
    initialFilledCount: Int = 0,
    private val loaded: Boolean = false,
) : MapRepository {
    private val filledCount = MutableStateFlow(initialFilledCount)

    override fun observeMaps(): Flow<List<MapWithProgress>> = filledCount.map { listOf(MapWithProgress(TwoCells, it)) }

    override fun observeMap(id: MapId): Flow<MapWithProgress> = filledCount.map { MapWithProgress(TwoCells, it) }

    override fun mapDefinition(id: MapId): MapDefinition = TwoCells

    override fun loadedMap(id: MapId): MapWithProgress? =
        if (loaded) MapWithProgress(TwoCells, filledCount.value) else null

    override suspend fun log(id: MapId, count: Int): List<Int> {
        if (count <= 0) return emptyList()
        val before = filledCount.value
        filledCount.value = (before + count).coerceAtMost(TwoCells.cells.size)
        return (before until filledCount.value).toList()
    }
}

private fun mapViewModel(repository: MapRepository = FakeMapRepository()) = MapViewModel(repository)

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @Test
    fun `collecting cells exposes ordered state with next marked`() =
        runTest {
            val viewModel = mapViewModel()

            viewModel.testWithInternalState(this) {
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
                        isLoaded = true,
                    )
                }
                collecting.cancel()
            }
        }

    @Test
    fun `a Map whose progress is not at hand yet opens as its outline until it loads`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 1))

            viewModel.testWithInternalState(this) {
                assertEquals(
                    MapUiState(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = false, isNext = false),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = false),
                            ),
                        filledCount = 0,
                        totalCount = 2,
                        isLoaded = false,
                    ),
                    viewModel.container.stateFlow.value,
                )
                val collecting = runOnCreate()
                expectInternalState {
                    copy(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = true, isNext = false),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = true),
                            ),
                        filledCount = 1,
                        isLoaded = true,
                    )
                }
                collecting.cancel()
            }
        }

    @Test
    fun `a Map whose progress is at hand opens in it without waiting`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 1, loaded = true))

            viewModel.testWithInternalState(this) {
                assertEquals(
                    MapUiState(
                        cells =
                            listOf(
                                CellUi(orderIndex = 0, row = 0, col = 0, filled = true, isNext = false),
                                CellUi(orderIndex = 1, row = 0, col = 1, filled = false, isNext = true),
                            ),
                        filledCount = 1,
                        totalCount = 2,
                        isLoaded = true,
                    ),
                    viewModel.container.stateFlow.value,
                )
                val collecting = runOnCreate()
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `logPushUps with one fills the next cell`() =
        runTest {
            val viewModel = mapViewModel()

            viewModel.testWithInternalState(this) {
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
                        isLoaded = true,
                    )
                }
                collecting.cancel()
            }
        }

    @Test
    fun `logPushUps with count fills that many next cells`() =
        runTest {
            val viewModel = mapViewModel()

            viewModel.testWithInternalState(this) {
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
                        isLoaded = true,
                    )
                }
                collecting.cancel()
            }
        }

    @Test
    fun `a Log on a partial Map emits exactly the Cells it filled`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 1))

            viewModel.testWithInternalState(this) {
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
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 1))

            viewModel.testWithInternalState(this) {
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
            val viewModel = mapViewModel()

            viewModel.testWithInternalState(this) {
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
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 2))

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `loading an empty Map emits nothing`() = assertLoadingEmitsNothing(filledCount = 0)

    @Test
    fun `loading a partial Map emits nothing`() = assertLoadingEmitsNothing(filledCount = 1)

    @Test
    fun `loading a complete Map emits nothing`() = assertLoadingEmitsNothing(filledCount = 2)

    private fun assertLoadingEmitsNothing(filledCount: Int) =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(filledCount))

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                skipItems(1)
                expectNoItems()
                collecting.cancel()
            }
        }
}
