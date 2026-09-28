package dev.stekl0.mapmethod.core.model

/** A Map as defined in code: its identity, its mask as [cells] in fill order and the [flag] it takes at Completion. */
public data class MapDefinition(
    val id: MapId,
    val cells: List<Cell>,
    val flag: Flag,
)
