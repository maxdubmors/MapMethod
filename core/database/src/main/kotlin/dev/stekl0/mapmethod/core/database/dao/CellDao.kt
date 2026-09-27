package dev.stekl0.mapmethod.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import dev.stekl0.mapmethod.core.database.model.CellEntity
import kotlinx.coroutines.flow.Flow

@Dao
public interface CellDao {
    @Query("SELECT * FROM cells ORDER BY orderIndex")
    public fun observeCells(): Flow<List<CellEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public suspend fun insertCells(cells: List<CellEntity>)

    /**
     * Fills up to [count] next empty Cells as one transaction; returns their fill order indexes in
     * fill order, none when the Map is already full.
     */
    @Transaction
    public suspend fun fillNext(count: Int, filledAt: Long): List<Int> {
        val next = nextEmptyOrderIndexes(count)
        if (next.isNotEmpty()) markNextFilled(count, filledAt)
        return next
    }

    @Query("SELECT orderIndex FROM cells WHERE filledAt IS NULL ORDER BY orderIndex LIMIT :count")
    public suspend fun nextEmptyOrderIndexes(count: Int): List<Int>

    // The same selection as nextEmptyOrderIndexes, so no bound IN list can outgrow SQLite's variable limit.
    @Query(
        "UPDATE cells SET filledAt = :filledAt " +
            "WHERE orderIndex IN (SELECT orderIndex FROM cells WHERE filledAt IS NULL " +
            "ORDER BY orderIndex LIMIT :count)",
    )
    public suspend fun markNextFilled(count: Int, filledAt: Long): Int
}
