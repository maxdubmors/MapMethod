package dev.stekl0.mapmethod.core.model

/** A Map as defined in code: its identity, its mask as [cells] in fill order and the [flag] it takes at Completion. */
public data class MapDefinition(
    val id: MapId,
    val cells: List<Cell>,
    val flag: Flag,
) {
    /** Rows of the grid the Map is drawn on, from row 0 to its southernmost Cell. */
    public val rows: Int = (cells.maxOfOrNull { it.row } ?: -1) + 1

    /** Columns of the grid the Map is drawn on, from column 0 to its easternmost Cell. */
    public val cols: Int = (cells.maxOfOrNull { it.col } ?: -1) + 1
}
