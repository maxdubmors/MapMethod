package dev.stekl0.mapmethod.core.database.model

import dev.stekl0.mapmethod.core.database.dao.FakeCellDao
import dev.stekl0.mapmethod.core.database.seedDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class CellSeederTest {
    @Test
    public fun `seeding inserts the mask cells`() =
        runTest {
            val dao = FakeCellDao()

            seedDatabase(dao)

            var received = emptyList<CellEntity>()
            val job = launch { dao.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertEquals(PolandMask.entities(), received)
            job.cancel()
        }

    @Test
    public fun `seeding twice keeps a single copy`() =
        runTest {
            val dao = FakeCellDao()

            seedDatabase(dao)
            seedDatabase(dao)

            var received = emptyList<CellEntity>()
            val job = launch { dao.observeCells().collect { received = it } }
            testScheduler.advanceUntilIdle()

            assertEquals(PolandMask.entities(), received)
            job.cancel()
        }

    @Test
    public fun `mask order is dense row-major north to south west to east`() {
        val entities = PolandMask.entities()

        assertTrue(entities.isNotEmpty())
        assertEquals(entities.indices.toList(), entities.map { it.orderIndex })
        val byOrder = entities.sortedBy { it.orderIndex }
        assertEquals(byOrder, byOrder.sortedWith(compareBy({ it.row }, { it.col })))
    }
}
