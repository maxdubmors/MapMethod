package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.runtime.Immutable
import dev.maxdubmors.mapmethod.core.model.LogOutcome

/**
 * One-shot events of the Map. [MapUiState] alone still describes what is filled, so a missed event
 * only costs its animation.
 */
@Immutable
public sealed interface MapEvent {
    /** A Log just filled Cells: [outcome] holds the Map before and after it, and whether it brought Completion. */
    public data class Logged(
        val outcome: LogOutcome,
    ) : MapEvent
}
