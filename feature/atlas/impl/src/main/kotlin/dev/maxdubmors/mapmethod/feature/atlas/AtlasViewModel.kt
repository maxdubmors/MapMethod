package dev.maxdubmors.mapmethod.feature.atlas

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maxdubmors.mapmethod.core.data.repository.MapRepository
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

/** Shows every Map with its progress, kept current while a Map opened from the Atlas logs on it. */
@HiltViewModel
public class AtlasViewModel
    @Inject
    constructor(
        repository: MapRepository,
    ) : ViewModel(), OrbitContainerHost<AtlasUiState, AtlasUiState, Nothing> {
        // Progress is read from app start on, so the Atlas normally opens on it without waiting.
        override val container: OrbitContainer<AtlasUiState, AtlasUiState, Nothing> =
            orbitContainer(initialState = (repository.loadedMaps() ?: emptyList()).toUiState()) {
                repository.observeMaps().collect { maps -> reduce { maps.toUiState() } }
            }
    }

private fun List<MapWithProgress>.toUiState(): AtlasUiState = AtlasUiState(pages = map(::AtlasPage))
