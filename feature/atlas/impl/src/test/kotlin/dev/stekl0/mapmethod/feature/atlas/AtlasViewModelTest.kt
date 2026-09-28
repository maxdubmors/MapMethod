package dev.stekl0.mapmethod.feature.atlas

import dev.stekl0.mapmethod.core.data.repository.MapRepository
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState
import dev.stekl0.mapmethod.core.ui.R as UiR

private val France =
    MapDefinition(
        id = MapId("france"),
        cells = listOf(Cell(orderIndex = 0, row = 0, col = 0), Cell(orderIndex = 1, row = 0, col = 1)),
        flag = Flag(bands = listOf(0xFF000001.toInt(), 0xFF000002.toInt())),
    )

// A second identity after France in the catalogue; it has no name, so its page never reads one.
private val OtherFrance = France.copy(id = MapId("other"))

/**
 * The catalogue [definitions] in order, each with the filled count in [initialFilledCounts]; their
 * progress is at hand without waiting once [loaded].
 */
private class FakeMapRepository(
    private val definitions: List<MapDefinition> = listOf(France),
    initialFilledCounts: Map<MapId, Int> = emptyMap(),
    private val loaded: Boolean = false,
) : MapRepository {
    val filledCounts = MutableStateFlow(initialFilledCounts)

    private fun List<MapDefinition>.withProgress(counts: Map<MapId, Int>) =
        map { MapWithProgress(it, counts[it.id] ?: 0) }

    override fun observeMaps(): Flow<List<MapWithProgress>> = filledCounts.map { definitions.withProgress(it) }

    override fun loadedMaps(): List<MapWithProgress>? =
        if (loaded) definitions.withProgress(filledCounts.value) else null

    override fun mapDefinition(id: MapId): MapDefinition = definitions.single { it.id == id }

    override fun observeMap(id: MapId): Flow<MapWithProgress> = error("The Atlas observes every Map at once")

    override fun loadedMap(id: MapId): MapWithProgress? = error("The Atlas observes every Map at once")

    override suspend fun log(id: MapId, count: Int): List<Int> = error("The Atlas never logs")
}

class AtlasViewModelTest {
    @Test
    fun `the Atlas lists every Map in catalogue order with its identity, name and progress`() =
        runTest {
            val repository =
                FakeMapRepository(
                    definitions = listOf(France, OtherFrance),
                    initialFilledCounts = mapOf(OtherFrance.id to 1),
                )
            val viewModel = AtlasViewModel(repository)

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                expectInternalState {
                    AtlasUiState(
                        pages =
                            listOf(AtlasPage(MapWithProgress(France, 0)), AtlasPage(MapWithProgress(OtherFrance, 1))),
                    )
                }
                collecting.cancel()
            }
            val pages = viewModel.container.stateFlow.value.pages
            assertEquals(listOf(France.id, OtherFrance.id), pages.map { it.id })
            assertEquals(listOf(0, 1), pages.map { it.filledCount })
            assertEquals(UiR.string.core_ui_map_name_france, pages.first().nameRes)
        }

    @Test
    fun `the Atlas opens on progress already read, without waiting`() =
        runTest {
            val repository = FakeMapRepository(initialFilledCounts = mapOf(France.id to 1), loaded = true)
            val viewModel = AtlasViewModel(repository)

            val page = viewModel.container.stateFlow.value.pages.single()

            assertEquals(France.id, page.id)
            assertEquals(1, page.filledCount)
            assertEquals(2, page.totalCount)
        }

    @Test
    fun `the Atlas is empty until progress is read`() =
        runTest {
            val viewModel = AtlasViewModel(FakeMapRepository())

            assertEquals(AtlasUiState(pages = emptyList()), viewModel.container.stateFlow.value)
        }

    @Test
    fun `progress logged on a Map flows through to its page`() =
        runTest {
            val repository = FakeMapRepository()
            val viewModel = AtlasViewModel(repository)

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                expectInternalState { AtlasUiState(pages = listOf(francePage(filledCount = 0))) }
                repository.filledCounts.value = mapOf(France.id to 1)
                expectInternalState { AtlasUiState(pages = listOf(francePage(filledCount = 1))) }
                collecting.cancel()
            }
        }

    @Test
    fun `a complete Map is marked complete and a partial one is not`() =
        runTest {
            val repository = FakeMapRepository(initialFilledCounts = mapOf(France.id to 1), loaded = true)
            val viewModel = AtlasViewModel(repository)

            viewModel.testWithInternalState(this) {
                val collecting = runOnCreate()
                assertFalse(viewModel.container.stateFlow.value.pages.single().isComplete)
                repository.filledCounts.value = mapOf(France.id to 2)
                expectInternalState { AtlasUiState(pages = listOf(francePage(filledCount = 2))) }
                assertTrue(viewModel.container.stateFlow.value.pages.single().isComplete)
                collecting.cancel()
            }
        }

    private fun francePage(filledCount: Int) =
        AtlasPage(map = MapWithProgress(France, filledCount))
}
