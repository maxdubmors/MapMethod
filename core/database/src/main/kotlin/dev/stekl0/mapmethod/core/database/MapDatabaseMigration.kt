package dev.stekl0.mapmethod.core.database

import androidx.room3.migration.Migration
import dev.stekl0.mapmethod.core.database.model.CellEntity
import dev.stekl0.mapmethod.core.database.model.PolandMask

/**
 * Replaces the mask rows with [newCells] while preserving the filled count:
 * the first cells take the old filled timestamps in fill order.
 */
internal fun reseedPreservingFill(oldFilledAt: List<Long>, newCells: List<CellEntity>): List<CellEntity> {
    val kept = oldFilledAt.take(newCells.size)
    return newCells.mapIndexed { index, cell -> cell.copy(filledAt = kept.getOrNull(index)) }
}

/** Version 1 to 2: same schema, new mask rows with the filled count preserved. */
internal val MapDatabaseMigration1To2: Migration =
    // Android-only sync driver APIs: Room runs migrations on its query
    // dispatcher inside a transaction, so blocking prepare/step is fine.
    Migration(1, 2) { connection ->
        val filled = mutableListOf<Long>()
        connection.prepare(
            "SELECT filledAt FROM cells WHERE filledAt IS NOT NULL ORDER BY orderIndex",
        ).use { stmt ->
            while (stmt.step()) filled.add(stmt.getLong(0))
        }
        connection.prepare("DELETE FROM cells").use { it.step() }
        reseedPreservingFill(filled, PolandMask.entities()).forEach { cell ->
            connection.prepare(
                "INSERT INTO cells (`orderIndex`, `row`, `col`, filledAt) VALUES (?, ?, ?, ?)",
            ).use { stmt ->
                stmt.bindLong(1, cell.orderIndex.toLong())
                stmt.bindLong(2, cell.row.toLong())
                stmt.bindLong(3, cell.col.toLong())
                if (cell.filledAt == null) stmt.bindNull(4) else stmt.bindLong(4, cell.filledAt)
                stmt.step()
            }
        }
    }
