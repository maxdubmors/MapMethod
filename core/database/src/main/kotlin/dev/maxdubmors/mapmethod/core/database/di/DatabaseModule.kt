package dev.maxdubmors.mapmethod.core.database.di

import android.content.Context
import androidx.room3.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.maxdubmors.mapmethod.core.database.MapDatabase
import dev.maxdubmors.mapmethod.core.database.dao.MapProgressDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesMapDatabase(
        @ApplicationContext context: Context,
    ): MapDatabase =
        Room.databaseBuilder<MapDatabase>(
            context,
            "mapmethod-database",
        ).build()

    @Provides
    fun providesMapProgressDao(database: MapDatabase): MapProgressDao = database.mapProgressDao()
}
