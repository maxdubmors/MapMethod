package dev.stekl0.mapmethod.core.database.dao

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

/** Map progress by Map identity; a Map without a row has nothing filled. */
@Dao
public interface MapProgressDao {
    @Query("SELECT mapId, filledCount FROM map_progress")
    public fun observeFilledCounts(): Flow<
        Map<
            @MapColumn("mapId")
            String,
            @MapColumn("filledCount")
            Int,
        >,
    >

    @Query("SELECT filledCount FROM map_progress WHERE mapId = :mapId")
    public fun observeFilledCount(mapId: String): Flow<Int?>

    /**
     * A Log: adds [count] to the filled count of [mapId], clamped to its [cellCount], as one
     * transaction that creates the row when it is missing.
     */
    @Transaction
    public suspend fun addFilled(mapId: String, count: Int, cellCount: Int): FilledCountChange {
        insertEmpty(mapId)
        val before = filledCount(mapId)
        raiseFilledCount(mapId = mapId, count = count, cellCount = cellCount)
        return FilledCountChange(before = before, after = filledCount(mapId))
    }

    @Query("INSERT OR IGNORE INTO map_progress (mapId, filledCount) VALUES (:mapId, 0)")
    public suspend fun insertEmpty(mapId: String)

    @Query("SELECT filledCount FROM map_progress WHERE mapId = :mapId")
    public suspend fun filledCount(mapId: String): Int

    @Query(
        "UPDATE map_progress SET filledCount = MIN(filledCount + :count, :cellCount) WHERE mapId = :mapId",
    )
    public suspend fun raiseFilledCount(mapId: String, count: Int, cellCount: Int)
}

/** A Map's filled count [before] and [after] a Log. */
public data class FilledCountChange(
    val before: Int,
    val after: Int,
)
