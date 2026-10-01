package dev.maxdubmors.mapmethod.feature.map

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maxdubmors.mapmethod.core.data.repository.MapRepository
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import dev.maxdubmors.mapmethod.feature.map.api.MapNavKey
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/** Shows the Map its destination [key] names, and logs on that Map only. */
@HiltViewModel(assistedFactory = MapViewModel.Factory::class)
public class MapViewModel
    @AssistedInject
    constructor(
        @Assisted key: MapNavKey,
        private val repository: MapRepository,
    ) : ViewModel(), OrbitContainerHost<MapUiState, MapUiState, MapEvent> {
        // The key comes whole because Dagger cannot pass the MapId value class through an assisted factory.
        private val mapId = key.mapId

        // Progress is read from app start on, so the Map normally opens on it without waiting;
        // otherwise it opens as its outline until the progress is read.
        override val container: OrbitContainer<MapUiState, MapUiState, MapEvent> =
            orbitContainer(
                initialState =
                    repository.loadedMap(mapId)?.toUiState() ?: repository.mapDefinition(mapId).toOutlineUiState(),
            ) {
                repository.observeMap(mapId).collect { map -> reduce { map.toUiState() } }
            }

        public fun logPushUps(count: Int) {
            intent {
                val outcome = repository.log(mapId, count)
                if (outcome.filledCells.isNotEmpty()) postSideEffect(MapEvent.Logged(outcome))
            }
        }

        @AssistedFactory
        public interface Factory {
            public fun create(key: MapNavKey): MapViewModel
        }
    }

private fun MapWithProgress.toUiState(): MapUiState = MapUiState(map = this, isLoaded = true)

private fun MapDefinition.toOutlineUiState(): MapUiState =
    MapUiState(map = MapWithProgress(this, filledCount = 0), isLoaded = false)
