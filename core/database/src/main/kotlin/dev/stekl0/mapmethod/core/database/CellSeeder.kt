package dev.stekl0.mapmethod.core.database

import dev.stekl0.mapmethod.core.database.dao.CellDao
import dev.stekl0.mapmethod.core.database.model.FranceMask

/** Inserts the mask Cells; existing rows win, so seeding twice is a no-op. */
public suspend fun seedDatabase(cellDao: CellDao) {
    cellDao.insertCells(FranceMask.entities())
}
