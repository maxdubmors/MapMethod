package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.runtime.Immutable
import dev.maxdubmors.mapmethod.core.model.MapWithProgress

@Immutable
public data class MapUiState(
    /** The Map shown with its progress; nothing filled until the progress is read. */
    val map: MapWithProgress,
    /** The Map's progress has been read; before that its Cells show as an empty outline. */
    val isLoaded: Boolean,
) {
    /** Empty Cells a Log can fill; none before the progress is read. */
    public val emptyCount: Int get() = if (isLoaded) map.emptyCount else 0

    /** Completion: every Cell of a loaded Map is filled. */
    public val isComplete: Boolean get() = isLoaded && map.isComplete

    /**
     * How many next Cells to preview for the Log [entry]: none before the progress is read, and the
     * single next Cell while the entry is empty or invalid, so the Map never loses its preview while typing.
     */
    public fun previewCount(entry: Int?): Int = if (isLoaded) entry ?: 1 else 0
}
