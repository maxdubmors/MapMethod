package dev.stekl0.mapmethod.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import dev.stekl0.mapmethod.core.database.dao.CellDao
import dev.stekl0.mapmethod.core.database.model.CellEntity

@Database(
    entities = [CellEntity::class],
    version = 2,
    exportSchema = true,
)
public abstract class MapDatabase : RoomDatabase() {
    public abstract fun cellDao(): CellDao
}
