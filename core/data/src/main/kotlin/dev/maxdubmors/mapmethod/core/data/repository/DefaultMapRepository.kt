package dev.maxdubmors.mapmethod.core.data.repository

import dev.maxdubmors.mapmethod.core.data.catalogue.MapCatalogue
import dev.maxdubmors.mapmethod.core.data.di.ApplicationScope
import dev.maxdubmors.mapmethod.core.database.dao.MapProgressDao
import dev.maxdubmors.mapmethod.core.model.LogOutcome
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultMapRepository
    @Inject
    constructor(
        private val mapProgressDao: MapProgressDao,
        private val catalogue: MapCatalogue,
        @ApplicationScope scope: CoroutineScope,
    ) : MapRepository {
        // Filled counts by Map identity, read from construction on so a Map is at hand when a screen
        // opens; null until read. It trails each write by a moment, so observing reads Room afresh.
        private val loadedFilledCounts: StateFlow<Map<String, Int>?> =
            mapProgressDao.observeFilledCounts().stateIn(scope, SharingStarted.Eagerly, initialValue = null)

        override fun observeMaps(): Flow<List<MapWithProgress>> =
            mapProgressDao.observeFilledCounts()
                .map { counts -> catalogue.maps.map { it.withProgress(counts) } }
                .distinctUntilChanged()

        override fun loadedMaps(): List<MapWithProgress>? =
            loadedFilledCounts.value?.let { counts -> catalogue.maps.map { it.withProgress(counts) } }

        override fun mapDefinition(id: MapId): MapDefinition = catalogue[id]

        override fun observeMap(id: MapId): Flow<MapWithProgress> {
            val definition = catalogue[id]
            return mapProgressDao.observeFilledCount(id.value)
                .map { MapWithProgress(definition, filledCount = it ?: 0) }
                .distinctUntilChanged()
        }

        override fun loadedMap(id: MapId): MapWithProgress? {
            val definition = catalogue[id]
            return loadedFilledCounts.value?.let { definition.withProgress(it) }
        }

        override suspend fun log(id: MapId, count: Int): LogOutcome {
            val definition = catalogue[id]
            if (count <= 0) {
                // A Log of nothing writes nothing; the Map stays as it is.
                val filledCount = mapProgressDao.observeFilledCount(id.value).first() ?: 0
                val current = MapWithProgress(definition, filledCount)
                return LogOutcome(before = current, after = current)
            }
            val change = mapProgressDao.addFilled(id.value, count = count, cellCount = definition.cells.size)
            return LogOutcome(
                before = MapWithProgress(definition, filledCount = change.before),
                after = MapWithProgress(definition, filledCount = change.after),
            )
        }
    }

// A Map without stored progress has nothing filled.
private fun MapDefinition.withProgress(filledCounts: Map<String, Int>) =
    MapWithProgress(this, filledCount = filledCounts[id.value] ?: 0)
