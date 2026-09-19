package dev.stekl0.mapmethod.core.database

import dev.stekl0.mapmethod.core.database.model.CellEntity
import org.junit.Assert.assertEquals
import org.junit.Test

public class MapDatabaseMigrationTest {
    private fun newCells() =
        listOf(
            CellEntity(orderIndex = 0, row = 0, col = 1, filledAt = null),
            CellEntity(orderIndex = 1, row = 0, col = 2, filledAt = null),
            CellEntity(orderIndex = 2, row = 1, col = 0, filledAt = null),
            CellEntity(orderIndex = 3, row = 1, col = 1, filledAt = null),
        )

    @Test
    public fun `reseed stamps the first cells with the old timestamps in order`() {
        val reseeded = reseedPreservingFill(oldFilledAt = listOf(100L, 200L), newCells = newCells())

        assertEquals(listOf(100L, 200L, null, null), reseeded.map { it.filledAt })
        assertEquals(
            newCells().map { it.orderIndex to (it.row to it.col) },
            reseeded.map { it.orderIndex to (it.row to it.col) },
        )
    }

    @Test
    public fun `reseed with no fills leaves the new mask unfilled`() {
        val reseeded = reseedPreservingFill(oldFilledAt = emptyList(), newCells = newCells())

        assertEquals(listOf(null, null, null, null), reseeded.map { it.filledAt })
    }

    @Test
    public fun `reseed clamps when old fills exceed the new total`() {
        val reseeded =
            reseedPreservingFill(
                oldFilledAt = listOf(100L, 200L, 300L, 400L, 500L),
                newCells = newCells().take(2),
            )

        assertEquals(listOf(100L, 200L), reseeded.map { it.filledAt })
    }
}
