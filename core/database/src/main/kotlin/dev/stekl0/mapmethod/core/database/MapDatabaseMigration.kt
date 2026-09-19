package dev.stekl0.mapmethod.core.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val filled = mutableListOf<Long>()
            db.query("SELECT filledAt FROM cells WHERE filledAt IS NOT NULL ORDER BY orderIndex").use { cursor ->
                while (cursor.moveToNext()) filled.add(cursor.getLong(cursor.getColumnIndexOrThrow("filledAt")))
            }
            db.execSQL("DELETE FROM cells")
            reseedPreservingFill(filled, PolandMask.entities()).forEach { cell ->
                val values =
                    ContentValues().apply {
                        put("orderIndex", cell.orderIndex)
                        put("row", cell.row)
                        put("col", cell.col)
                        if (cell.filledAt == null) putNull("filledAt") else put("filledAt", cell.filledAt)
                    }
                db.insert("cells", SQLiteDatabase.CONFLICT_NONE, values)
            }
        }
    }
