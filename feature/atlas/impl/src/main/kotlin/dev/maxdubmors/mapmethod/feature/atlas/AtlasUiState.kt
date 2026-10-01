package dev.maxdubmors.mapmethod.feature.atlas

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import dev.maxdubmors.mapmethod.core.ui.mapNameRes

/**
 * One page of the Atlas: a Map with its progress, drawn as a preview. [nameResOverride] names a Map
 * the catalogue has no name for, such as a second Map made up by a test.
 */
@Immutable
public data class AtlasPage(
    val map: MapWithProgress,
    @param:StringRes private val nameResOverride: Int? = null,
) {
    /** The country's name, looked up by the Map's identity unless given. */
    @get:StringRes
    public val nameRes: Int get() = nameResOverride ?: mapNameRes(map.id)
}

/** The Atlas: every Map in catalogue order, none until their progress is read. */
@Immutable
public data class AtlasUiState(
    val pages: List<AtlasPage>,
)
