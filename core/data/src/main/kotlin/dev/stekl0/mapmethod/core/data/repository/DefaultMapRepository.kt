package dev.stekl0.mapmethod.core.data.repository

import dev.stekl0.mapmethod.core.data.catalogue.MapCatalogue
import dev.stekl0.mapmethod.core.data.di.ApplicationScope
import dev.stekl0.mapmethod.core.database.dao.MapProgressDao
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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

        override suspend fun log(id: MapId, count: Int): List<Int> {
            if (count <= 0) return emptyList()
            val definition = catalogue[id]
            val change = mapProgressDao.addFilled(id.value, count = count, cellCount = definition.cells.size)
            return definition.cells.subList(change.before, change.after).map { it.orderIndex }
        }
    }

// A Map without stored progress has nothing filled.
private fun MapDefinition.withProgress(filledCounts: Map<String, Int>) =
    MapWithProgress(this, filledCount = filledCounts[id.value] ?: 0)
