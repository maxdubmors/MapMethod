package dev.stekl0.mapmethod.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.stekl0.mapmethod.core.data.catalogue.MapCatalogue
import dev.stekl0.mapmethod.core.data.repository.DefaultMapRepository
import dev.stekl0.mapmethod.core.data.repository.MapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindsMapRepository(impl: DefaultMapRepository): MapRepository

    companion object {
        @Provides
        fun providesMapCatalogue(): MapCatalogue = MapCatalogue.Default

        @Provides
        @Singleton
        @ApplicationScope
        fun providesApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
