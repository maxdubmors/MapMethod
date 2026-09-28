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
    /** The Map's progress has been read; before that its Cells show as an empty outline. */
    val isLoaded: Boolean,
) {
    /** Cells a Log can still fill; none before the progress is read. */
    public val remaining: Int get() = if (isLoaded) totalCount - filledCount else 0

    /** Completion: every Cell of a loaded Map is filled. */
    public val isComplete: Boolean get() = isLoaded && (filledCount == totalCount)
}
