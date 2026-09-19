package dev.stekl0.mapmethod.core.database.repository

import dev.stekl0.mapmethod.core.database.dao.CellDao
import dev.stekl0.mapmethod.core.database.model.toCell
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

public class MapRepositoryImpl
    @Inject
    constructor(
        private val cellDao: CellDao,
    ) : MapRepository {
        override fun observeCells(): Flow<List<dev.stekl0.mapmethod.core.database.model.Cell>> =
            cellDao.observeCells().map { entities -> entities.map { it.toCell() } }

        override suspend fun logPushUps(): Boolean =
            cellDao.fillNext(filledAt = System.currentTimeMillis()) == 1
    }
