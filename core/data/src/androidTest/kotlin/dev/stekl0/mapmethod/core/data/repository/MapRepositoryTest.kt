package dev.stekl0.mapmethod.core.data.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.stekl0.mapmethod.core.data.catalogue.France
import dev.stekl0.mapmethod.core.data.catalogue.FranceMapId
import dev.stekl0.mapmethod.core.data.catalogue.MapCatalogue
import dev.stekl0.mapmethod.core.database.InMemoryMapDatabase
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapRepositoryTest {
    private val database = InMemoryMapDatabase(ApplicationProvider.getApplicationContext())

    // A second Map beside France, to tell the progress of one identity from another's.
    private val pair =
        MapDefinition(
            id = MapId("pair"),
            cells = listOf(Cell(orderIndex = 0, row = 0, col = 0), Cell(orderIndex = 1, row = 0, col = 1)),
        )

    private fun TestScope.repository(catalogue: MapCatalogue = MapCatalogue.Default): MapRepository =
        DefaultMapRepository(database.mapProgressDao, catalogue, backgroundScope)

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun aFreshMapObservesAsNothingFilled() =
        runTest {
            val france = repository().observeMap(FranceMapId).first()

            assertEquals(MapWithProgress(France, filledCount = 0), france)
            assertEquals(100, france.totalCount)
        }

    @Test
    fun aLogFillsTheNextCellsAndReturnsTheirFillOrderIndexes() =
        runTest {
            val repository = repository()

            assertEquals(listOf(0, 1, 2), repository.log(FranceMapId, 3))
            assertEquals(listOf(3, 4), repository.log(FranceMapId, 2))
            assertEquals(5, repository.observeMap(FranceMapId).first().filledCount)
        }

    @Test
    fun anObservedMapFollowsEachLog() =
        runTest {
            val repository = repository()
            val filledThree =
                async(start = CoroutineStart.UNDISPATCHED) {
                    repository.observeMap(FranceMapId).first { it.filledCount == 3 }
                }

            val filled = repository.log(FranceMapId, 3)

            assertEquals(listOf(0, 1, 2), filled)
            assertEquals(MapWithProgress(France, filledCount = 3), filledThree.await())
        }

    @Test
    fun aLogLargerThanWhatIsLeftClampsAndReturnsOnlyThoseCells() =
        runTest {
            val repository = repository()
            repository.log(FranceMapId, 97)

            assertEquals(listOf(97, 98, 99), repository.log(FranceMapId, 5))
            val france = repository.observeMap(FranceMapId).first()
            assertEquals(100, france.filledCount)
            assertTrue(france.isComplete)
        }

    @Test
    fun aLogOnACompleteMapReturnsNothing() =
        runTest {
            val repository = repository()
            repository.log(FranceMapId, 100)

            assertEquals(emptyList<Int>(), repository.log(FranceMapId, 1))
            assertEquals(100, repository.observeMap(FranceMapId).first().filledCount)
        }

    @Test
    fun aNonPositiveLogReturnsNothing() =
        runTest {
            val repository = repository()
            repository.log(FranceMapId, 4)

            assertEquals(emptyList<Int>(), repository.log(FranceMapId, 0))
            assertEquals(emptyList<Int>(), repository.log(FranceMapId, -3))
            assertEquals(4, repository.observeMap(FranceMapId).first().filledCount)
        }

    @Test
    fun theProgressOfOneMapDoesNotAffectAnother() =
        runTest {
            val repository = repository(MapCatalogue(MapCatalogue.Default.maps + pair))
            repository.log(FranceMapId, 60)

            assertEquals(MapWithProgress(pair, filledCount = 0), repository.observeMap(pair.id).first())
            assertEquals(listOf(0, 1), repository.log(pair.id, 5))
            assertEquals(60, repository.observeMap(FranceMapId).first().filledCount)
        }

    @Test
    fun observingAllMapsListsTheCatalogueInOrderWithProgress() =
        runTest {
            val repository = repository(MapCatalogue(listOf(pair) + MapCatalogue.Default.maps))
            repository.log(FranceMapId, 7)

            assertEquals(
                listOf(MapWithProgress(pair, filledCount = 0), MapWithProgress(France, filledCount = 7)),
                repository.observeMaps().first(),
            )
        }

    @Test
    fun observingAllMapsOnAFreshInstallListsFranceWithNothingFilled() =
        runTest {
            assertEquals(listOf(MapWithProgress(France, filledCount = 0)), repository().observeMaps().first())
        }

    @Test
    fun aMapWhoseProgressHasBeenReadIsAtHandWithoutWaiting() =
        runTest {
            val repository = repository()
            val observed = repository.observeMap(FranceMapId).first()

            assertEquals(observed, repository.loadedMap(FranceMapId))
        }

    @Test
    fun aMapAtHandFollowsEachLog() =
        runTest {
            val repository = repository()
            repository.log(FranceMapId, 3)
            repository.observeMap(FranceMapId).first { it.filledCount == 3 }

            assertEquals(MapWithProgress(France, filledCount = 3), repository.loadedMap(FranceMapId))
        }
}
