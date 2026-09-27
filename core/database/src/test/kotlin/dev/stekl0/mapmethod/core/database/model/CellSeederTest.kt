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

            assertEquals(FranceMask.entities(), received)
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

            assertEquals(FranceMask.entities(), received)
            job.cancel()
        }

    @Test
    fun `mask is thirteen rows by fourteen columns`() {
        assertEquals(13, FranceMask.rows.size)
        assertTrue(FranceMask.rows.all { it.length == 14 })
    }

    @Test
    fun `mask is cropped to its own bounds`() {
        val entities = FranceMask.entities()

        assertEquals(0, entities.minOf { it.row })
        assertEquals(12, entities.maxOf { it.row })
        assertEquals(0, entities.minOf { it.col })
        assertEquals(13, entities.maxOf { it.col })
    }

    @Test
    fun `mask holds the method's hundred cells`() {
        assertEquals(100, FranceMask.entities().size)
    }

    @Test
    fun `mask order is dense row-major north to south west to east`() {
        val entities = FranceMask.entities()

        assertTrue(entities.isNotEmpty())
        assertEquals(entities.indices.toList(), entities.map { it.orderIndex })
        val byOrder = entities.sortedBy { it.orderIndex }
        assertEquals(byOrder, byOrder.sortedWith(compareBy({ it.row }, { it.col })))
    }
}
