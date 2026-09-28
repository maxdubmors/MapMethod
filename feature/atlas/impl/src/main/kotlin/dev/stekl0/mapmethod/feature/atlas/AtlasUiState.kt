package dev.stekl0.mapmethod.feature.atlas

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress
import dev.stekl0.mapmethod.core.ui.mapNameRes

/** One page of the Atlas: a Map with its progress, drawn as a preview. */
@Immutable
public data class AtlasPage(
    val map: MapWithProgress,
) {
    public val id: MapId get() = map.id

    /** The country's name, looked up by the Map's identity. */
    @get:StringRes
    public val nameRes: Int get() = mapNameRes(id)

    public val filledCount: Int get() = map.filledCount

    public val totalCount: Int get() = map.totalCount

    /** Completion: the preview shows the Map in its flag colours. */
    public val isComplete: Boolean get() = map.isComplete
}

/** The Atlas: every Map in catalogue order, none until their progress is read. */
@Immutable
public data class AtlasUiState(
    val pages: List<AtlasPage>,
)
