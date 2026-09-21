package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.Immutable

@Immutable
public data class CellUi(
    val orderIndex: Int,
    val row: Int,
    val col: Int,
    val filled: Boolean,
    val isNext: Boolean,
)

@Immutable
public data class MapUiState(
    val cells: List<CellUi>,
    val filledCount: Int,
    val totalCount: Int,
) {
    public val isFull: Boolean get() = filledCount == totalCount

    public companion object {
        public val EMPTY: MapUiState = MapUiState(cells = emptyList(), filledCount = 0, totalCount = 0)
    }
}
