package dev.maxdubmors.mapmethod.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import dev.maxdubmors.mapmethod.core.database.dao.MapProgressDao
import dev.maxdubmors.mapmethod.core.database.model.MapProgressEntity

@Database(
    entities = [MapProgressEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class MapDatabase : RoomDatabase() {
    abstract fun mapProgressDao(): MapProgressDao
}
