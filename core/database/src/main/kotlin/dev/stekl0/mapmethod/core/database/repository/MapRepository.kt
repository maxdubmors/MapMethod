package dev.stekl0.mapmethod.core.database.repository

import dev.stekl0.mapmethod.core.database.model.Cell
import kotlinx.coroutines.flow.Flow

/** The single seam for Map progress: ordered Cells plus one Log action. */
public interface MapRepository {
    public fun observeCells(): Flow<List<Cell>>

    /** Fills the next empty Cell; returns false when the Map is already full. */
    public suspend fun logPushUps(): Boolean
}
