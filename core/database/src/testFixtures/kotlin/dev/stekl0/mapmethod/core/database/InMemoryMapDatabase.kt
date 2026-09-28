package dev.stekl0.mapmethod.core.database

import android.content.Context
import androidx.room3.Room
import dev.stekl0.mapmethod.core.database.dao.MapProgressDao

/**
 * A fresh, empty database held in memory, for tests of what is built on it; close it after each
 * test. The app gets its database through Hilt instead.
 */
public class InMemoryMapDatabase(
    context: Context,
) : AutoCloseable {
    private val database = Room.inMemoryDatabaseBuilder<MapDatabase>(context).build()

    public val mapProgressDao: MapProgressDao = database.mapProgressDao()

    override fun close() {
        database.close()
    }
}
