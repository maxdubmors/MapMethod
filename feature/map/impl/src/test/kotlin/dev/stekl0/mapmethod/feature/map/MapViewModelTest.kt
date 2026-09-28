package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.data.repository.MapRepository
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import dev.stekl0.mapmethod.feature.map.api.MapNavKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

private val TwoCells =
    MapDefinition(
        id = MapId("two"),
        cells = listOf(Cell(orderIndex = 0, row = 0, col = 0), Cell(orderIndex = 1, row = 0, col = 1)),
        flag = Flag(bands = listOf(0xFF000001.toInt(), 0xFF000002.toInt())),
    )

private val OtherTwoCells =
    TwoCells.copy(id = MapId("other"), flag = Flag(bands = listOf(0xFF000003.toInt(), 0xFF000004.toInt())))

/**
 * Two Maps, [TwoCells] and [OtherTwoCells], whose first [initialFilledCount] and [otherFilledCount] Cells are
 * filled; their progress is at hand without waiting once [loaded].
 */
private class FakeMapRepository(
    initialFilledCount: Int = 0,
    otherFilledCount: Int = 0,
    private val loaded: Boolean = false,
) : MapRepository {
    private val definitions = listOf(TwoCells, OtherTwoCells)
    private val filledCounts =
        MutableStateFlow(mapOf(TwoCells.id to initialFilledCount, OtherTwoCells.id to otherFilledCount))

    private fun progress(id: MapId, counts: Map<MapId, Int>) = MapWithProgress(mapDefinition(id), counts.getValue(id))

    override fun observeMaps(): Flow<List<MapWithProgress>> =
        filledCounts.map { counts -> definitions.map { progress(it.id, counts) } }

    override fun observeMap(id: MapId): Flow<MapWithProgress> =
        filledCounts.map { progress(id, it) }.distinctUntilChanged()

    override fun mapDefinition(id: MapId): MapDefinition = definitions.single { it.id == id }

    override fun loadedMap(id: MapId): MapWithProgress? = if (loaded) progress(id, filledCounts.value) else null

    override suspend fun log(id: MapId, count: Int): List<Int> {
        if (count <= 0) return emptyList()
        val before = filledCounts.value.getValue(id)
        val after = (before + count).coerceAtMost(mapDefinition(id).cells.size)
        filledCounts.value += id to after
        return (before until after).toList()
    }
}

private fun mapViewModel(
    repository: MapRepository = FakeMapRepository(),
    mapId: MapId = TwoCells.id,
) = MapViewModel(MapNavKey(mapId), repository)

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
    fun `the Map it was given opens as its outline until its progress loads`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(otherFilledCount = 1), mapId = OtherTwoCells.id)

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
                        flag = OtherTwoCells.flag,
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
    fun `the Map it was given opens in its progress without waiting when that is at hand`() =
        runTest {
            val viewModel =
                mapViewModel(FakeMapRepository(otherFilledCount = 1, loaded = true), mapId = OtherTwoCells.id)

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
                        flag = OtherTwoCells.flag,
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
    fun `a Log fills the Map it was given and leaves another Map untouched`() =
        runTest {
            val repository = FakeMapRepository()
            val viewModel = mapViewModel(repository, mapId = OtherTwoCells.id)

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectSideEffect(MapEvent.LogFilled(orderIndexes = listOf(0)))
                skipItems(1)
                collecting.cancel()
            }
            assertEquals(1, repository.observeMap(OtherTwoCells.id).first().filledCount)
            assertEquals(0, repository.observeMap(TwoCells.id).first().filledCount)
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
