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
     * Fills the next empty Cell in one statement; returns 1 when a Cell was
     * filled, 0 when the Map is already full.
     */
    @Query(
        "UPDATE cells SET filledAt = :filledAt " +
            "WHERE orderIndex = (SELECT MIN(orderIndex) FROM cells WHERE filledAt IS NULL)",
    )
    public suspend fun fillNext(filledAt: Long): Int
}
