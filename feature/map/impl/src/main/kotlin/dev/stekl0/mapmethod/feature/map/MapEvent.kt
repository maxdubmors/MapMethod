package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.Immutable

/**
 * One-shot events of the Map. [MapUiState] alone still describes what is filled, so a missed event
 * only costs its animation.
 */
@Immutable
public sealed interface MapEvent {
    /** A Log just filled the Cells at [orderIndexes], in fill order. */
    public data class LogFilled(
        val orderIndexes: List<Int>,
    ) : MapEvent
}
