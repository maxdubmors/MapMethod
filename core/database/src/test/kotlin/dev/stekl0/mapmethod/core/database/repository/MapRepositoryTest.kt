package dev.stekl0.mapmethod.core.database.repository

import dev.stekl0.mapmethod.core.database.dao.FakeCellDao
import dev.stekl0.mapmethod.core.database.model.Cell
import dev.stekl0.mapmethod.core.database.model.CellEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class MapRepositoryTest {
    private fun entities() =
        listOf(
            CellEntity(orderIndex = 0, row = 0, col = 1, filledAt = null),
            CellEntity(orderIndex = 1, row = 0, col = 2, filledAt = null),
            CellEntity(orderIndex = 2, row = 1, col = 0, filledAt = null),
        )

    @Test
    public fun `observeCells maps entities in fill order`() =
        runTest {
            val repository: MapRepository = MapRepositoryImpl(FakeCellDao(entities()))

            var received = emptyList<Cell>()
            val job = launch { repository.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(0, 1, 2), received.map { it.orderIndex })
            assertTrue(received.none { it.filled })
            job.cancel()
        }

    @Test
    public fun `logPushUps with count fills that many lowest unfilled cells`() =
        runTest {
            val dao = FakeCellDao(entities())
            val repository: MapRepository = MapRepositoryImpl(dao)

            assertEquals(2, repository.logPushUps(2))

            var received = emptyList<Cell>()
            val job = launch { repository.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(true, true, false), received.map { it.filled })
            job.cancel()
        }

    @Test
    public fun `logPushUps beyond remaining fills only what is left`() =
        runTest {
            val dao = FakeCellDao(entities())
            val repository: MapRepository = MapRepositoryImpl(dao)

            assertEquals(3, repository.logPushUps(5))

            var received = emptyList<Cell>()
            val job = launch { repository.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertTrue(received.all { it.filled })
            job.cancel()
        }

    @Test
    public fun `logPushUps with non-positive count fills nothing`() =
        runTest {
            val dao = FakeCellDao(entities())
            val repository: MapRepository = MapRepositoryImpl(dao)

            assertEquals(0, repository.logPushUps(0))

            var received = emptyList<Cell>()
            val job = launch { repository.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertTrue(received.none { it.filled })
            job.cancel()
        }

    @Test
    public fun `logPushUps on a full map returns zero without writing`() =
        runTest {
            val dao = FakeCellDao(listOf(CellEntity(orderIndex = 0, row = 0, col = 0, filledAt = 1L)))
            val repository: MapRepository = MapRepositoryImpl(dao)

            assertEquals(0, repository.logPushUps(3))
            assertEquals(1, dao.fillCalls)
        }
}
