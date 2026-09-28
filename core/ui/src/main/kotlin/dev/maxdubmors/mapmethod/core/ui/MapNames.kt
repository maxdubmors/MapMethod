package dev.maxdubmors.mapmethod.core.ui

import androidx.annotation.StringRes
import dev.maxdubmors.mapmethod.core.model.MapId

/** The country name of the Map [id], as the user reads it; every Map in the catalogue has one. */
@StringRes
public fun mapNameRes(id: MapId): Int =
    when (id.value) {
        "france" -> R.string.core_ui_map_name_france
        else -> error("No name for Map ${id.value}")
    }
