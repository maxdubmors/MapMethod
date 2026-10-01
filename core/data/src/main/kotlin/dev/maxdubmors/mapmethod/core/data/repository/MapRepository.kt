package dev.maxdubmors.mapmethod.core.data.repository

import dev.maxdubmors.mapmethod.core.model.LogOutcome
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.flow.Flow

/** The single seam for Maps: the catalogue joined with the user's progress on each Map. */
public interface MapRepository {
    /** Every Map with its progress, in catalogue order. */
    public fun observeMaps(): Flow<List<MapWithProgress>>

    /**
     * Every Map with its progress, in catalogue order, when that progress has already been read, so a
     * screen can open on it without waiting; null before the first read, as for [loadedMap].
     */
    public fun loadedMaps(): List<MapWithProgress>?

    /** The Map [id] as the catalogue defines it, at hand at once. */
    public fun mapDefinition(id: MapId): MapDefinition

    /** The Map [id] with its progress. */
    public fun observeMap(id: MapId): Flow<MapWithProgress>

    /**
     * The Map [id] with its progress when that progress has already been read, so a screen can open
     * on it without waiting; null before the first read. Progress is read from app start on.
     */
    public fun loadedMap(id: MapId): MapWithProgress?

    /**
     * A Log of [count] on the Map [id]: fills up to [count] of its next empty Cells. Returns the Map
     * before and after it; the two are the same when [count] is not positive or the Map is complete.
     */
    public suspend fun log(id: MapId, count: Int): LogOutcome
}
