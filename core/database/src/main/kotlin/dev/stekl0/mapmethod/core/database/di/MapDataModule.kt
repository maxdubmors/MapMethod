package dev.stekl0.mapmethod.core.database.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.stekl0.mapmethod.core.database.repository.MapRepository
import dev.stekl0.mapmethod.core.database.repository.MapRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MapDataModule {
    @Binds
    abstract fun bindsMapRepository(impl: MapRepositoryImpl): MapRepository
}
