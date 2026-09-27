package dev.stekl0.mapmethod.feature.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.stekl0.mapmethod.core.database.model.Cell
import dev.stekl0.mapmethod.core.database.repository.MapRepository
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
        override val container: OrbitContainer<MapUiState, MapUiState, MapEvent> =
            orbitContainer(initialState = MapUiState.EMPTY) {
                repository.observeCells().collect { cells -> reduce { cells.toUiState() } }
            }

        public fun logPushUps(count: Int) {
            intent {
                val filled = repository.logPushUps(count)
                if (filled.isNotEmpty()) postSideEffect(MapEvent.LogFilled(orderIndexes = filled))
            }
        }
    }

private fun List<Cell>.toUiState(): MapUiState {
    val next = firstOrNull { !it.filled }?.orderIndex
    return MapUiState(
        cells =
            map { cell ->
                CellUi(
                    orderIndex = cell.orderIndex,
                    row = cell.row,
                    col = cell.col,
                    filled = cell.filled,
                    isNext = cell.orderIndex == next,
                )
            },
        filledCount = count { it.filled },
        totalCount = size,
    )
}
