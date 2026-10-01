package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.data.repository.MapRepository
import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.LogOutcome
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import dev.maxdubmors.mapmethod.feature.map.api.MapNavKey
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

    override fun loadedMaps(): List<MapWithProgress>? =
        if (loaded) definitions.map { progress(it.id, filledCounts.value) } else null

    override fun loadedMap(id: MapId): MapWithProgress? = if (loaded) progress(id, filledCounts.value) else null

    override suspend fun log(id: MapId, count: Int): LogOutcome {
        val before = filledCounts.value.getValue(id)
        val after = (before + count.coerceAtLeast(0)).coerceAtMost(mapDefinition(id).cells.size)
        filledCounts.value += id to after
        return LogOutcome(MapWithProgress(mapDefinition(id), before), MapWithProgress(mapDefinition(id), after))
    }
}

private fun logged(map: MapDefinition, before: Int, after: Int) =
    MapEvent.Logged(LogOutcome(MapWithProgress(map, before), MapWithProgress(map, after)))

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
                expectInternalState { copy(isLoaded = true) }
                collecting.cancel()
            }
        }

    @Test
    fun `the Map it was given opens as its outline until its progress loads`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(otherFilledCount = 1), mapId = OtherTwoCells.id)

            viewModel.testWithInternalState(this) {
                assertEquals(
                    MapUiState(map = MapWithProgress(OtherTwoCells, filledCount = 0), isLoaded = false),
                    viewModel.container.stateFlow.value,
                )
                val collecting = runOnCreate()
                expectInternalState {
                    MapUiState(map = MapWithProgress(OtherTwoCells, filledCount = 1), isLoaded = true)
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
                    MapUiState(map = MapWithProgress(OtherTwoCells, filledCount = 1), isLoaded = true),
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
                expectSideEffect(logged(TwoCells, before = 0, after = 1))
                expectInternalState { copy(map = MapWithProgress(TwoCells, filledCount = 1), isLoaded = true) }
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
                expectSideEffect(logged(TwoCells, before = 0, after = 2))
                expectInternalState { copy(map = MapWithProgress(TwoCells, filledCount = 2), isLoaded = true) }
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
                expectSideEffect(logged(TwoCells, before = 1, after = 2))
                skipItems(1)
                collecting.cancel()
            }
        }

    @Test
    fun `a Log after the one that completed the Map emits nothing`() =
        runTest {
            val viewModel = mapViewModel(FakeMapRepository(initialFilledCount = 1))

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectSideEffect(logged(TwoCells, before = 1, after = 2))
                skipItems(1)
                viewModel.logPushUps(1)
                expectNoItems()
                collecting.cancel()
            }
        }

    @Test
    fun `a Log that leaves Cells empty emits only its own outcome`() =
        runTest {
            val viewModel = mapViewModel()

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                skipItems(1)
                viewModel.logPushUps(1)
                expectSideEffect(logged(TwoCells, before = 0, after = 1))
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
                expectSideEffect(logged(OtherTwoCells, before = 0, after = 1))
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
