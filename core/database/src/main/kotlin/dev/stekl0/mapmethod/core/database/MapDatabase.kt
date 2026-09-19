package dev.stekl0.mapmethod.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
