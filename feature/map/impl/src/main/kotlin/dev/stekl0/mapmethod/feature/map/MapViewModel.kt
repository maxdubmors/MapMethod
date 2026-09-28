package dev.stekl0.mapmethod.feature.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stekl0.mapmethod.core.data.catalogue.FranceMapId
import dev.stekl0.mapmethod.core.data.repository.MapRepository
import dev.stekl0.mapmethod.core.model.MapWithProgress
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
public class MapViewModel
    @Inject
    constructor(
        private val repository: MapRepository,
    ) : ViewModel(), OrbitContainerHost<MapUiState, MapUiState, MapEvent> {
        // The Map destination names no Map yet, so it shows France.
        private val mapId = FranceMapId

        // Progress is read from app start on, so the Map normally opens on it without waiting.
        override val container: OrbitContainer<MapUiState, MapUiState, MapEvent> =
            orbitContainer(initialState = repository.loadedMap(mapId)?.toUiState() ?: MapUiState.EMPTY) {
                repository.observeMap(mapId).collect { map -> reduce { map.toUiState() } }
            }

        public fun logPushUps(count: Int) {
            intent {
                val filled = repository.log(mapId, count)
                if (filled.isEmpty()) return@intent
                postSideEffect(MapEvent.LogFilled(orderIndexes = filled))
                // Cells fill in fill order, so the Log that fills the last one completes the Map.
                if (filled.last() == state.cells.maxOfOrNull { it.orderIndex }) postSideEffect(MapEvent.Completion)
            }
        }
    }

// The first filledCount Cells in fill order are filled, and the one after them is next.
private fun MapWithProgress.toUiState(): MapUiState =
    MapUiState(
        cells =
            cells.mapIndexed { position, cell ->
                CellUi(
                    orderIndex = cell.orderIndex,
                    row = cell.row,
                    col = cell.col,
                    filled = position < filledCount,
                    isNext = position == filledCount,
                )
            },
        filledCount = filledCount,
        totalCount = totalCount,
    )
