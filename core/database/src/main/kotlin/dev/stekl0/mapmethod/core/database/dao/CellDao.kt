package dev.stekl0.mapmethod.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.stekl0.mapmethod.core.database.model.CellEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface CellDao {
    @Query("SELECT * FROM cells ORDER BY orderIndex")
    public fun observeCells(): Flow<List<CellEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertCells(cells: List<CellEntity>)

    /**
     * Fills up to [count] next empty Cells in one statement; returns how many
     * Cells were filled, 0 when the Map is already full.
     */
    @Query(
        "UPDATE cells SET filledAt = :filledAt " +
            "WHERE orderIndex IN (SELECT orderIndex FROM cells WHERE filledAt IS NULL " +
            "ORDER BY orderIndex LIMIT :count)",
    )
    public suspend fun fillNext(count: Int, filledAt: Long): Int
}
