package dev.stekl0.mapmethod.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.stekl0.mapmethod.core.database.MapDatabase
import dev.stekl0.mapmethod.core.database.dao.CellDao
import dev.stekl0.mapmethod.core.database.seedDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesMapDatabase(
        @ApplicationContext context: Context,
    ): MapDatabase {
        val database =
            Room.databaseBuilder(
                context,
                MapDatabase::class.java,
                "mapmethod-database",
            ).build()
        // Prefill once: inserts ignore existing rows, so this is a no-op
        // on every launch after the first. Runs off the main thread.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            seedDatabase(database.cellDao())
        }
        return database
    }

    @Provides
    fun providesCellDao(database: MapDatabase): CellDao = database.cellDao()
}
