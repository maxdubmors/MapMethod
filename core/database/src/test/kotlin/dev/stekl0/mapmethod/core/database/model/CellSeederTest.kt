package dev.stekl0.mapmethod.core.database.model

import dev.stekl0.mapmethod.core.database.dao.FakeCellDao
import dev.stekl0.mapmethod.core.database.seedDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CellSeederTest {
    @Test
    fun `seeding inserts the mask cells`() =
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
    fun `seeding twice keeps a single copy`() =
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
    fun `mask is sixty by sixty over the same bounding box`() {
        assertEquals(60, PolandMask.rows.size)
        assertTrue(PolandMask.rows.all { it.length == 60 })
    }

    @Test
    fun `mask holds on the order of two to three thousand cells`() {
        val total = PolandMask.entities().size

        assertTrue(total in (2000..3000))
    }

    @Test
    fun `mask order is dense row-major north to south west to east`() {
        val entities = PolandMask.entities()

        assertTrue(entities.isNotEmpty())
        assertEquals(entities.indices.toList(), entities.map { it.orderIndex })
        val byOrder = entities.sortedBy { it.orderIndex }
        assertEquals(byOrder, byOrder.sortedWith(compareBy({ it.row }, { it.col })))
    }
}
