package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.Immutable
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.model.MapWithProgress

@Immutable
public data class MapUiState(
    /** The Map shown with its progress; nothing filled until the progress is read. */
    val map: MapWithProgress,
    /** The Map's progress has been read; before that its Cells show as an empty outline. */
    val isLoaded: Boolean,
) {
    /** The Map's Cells in fill order; the first [filledCount] of them are filled. */
    public val cells: List<Cell> get() = map.cells

    public val filledCount: Int get() = map.filledCount

    public val totalCount: Int get() = map.totalCount

    /** The flag of the Map shown, its colours at Completion. */
    public val flag: Flag get() = map.definition.flag

    /** Cells a Log can still fill; none before the progress is read. */
    public val remaining: Int get() = if (isLoaded) totalCount - filledCount else 0

    /** Completion: every Cell of a loaded Map is filled. */
    public val isComplete: Boolean get() = isLoaded && (filledCount == totalCount)
}
