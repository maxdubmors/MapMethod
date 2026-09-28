package dev.stekl0.mapmethod.core.model

/** A Map as defined in code: its identity and its mask as [cells] in fill order. */
public data class MapDefinition(
    val id: MapId,
    val cells: List<Cell>,
)
