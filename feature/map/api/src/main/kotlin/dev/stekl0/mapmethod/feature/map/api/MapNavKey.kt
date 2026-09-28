package dev.stekl0.mapmethod.feature.map.api

import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.core.model.MapId
import kotlinx.serialization.Serializable

/** The Map destination, showing the Map [mapId]. */
@Serializable
public data class MapNavKey(
    val mapId: MapId,
) : NavKey
