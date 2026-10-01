package dev.maxdubmors.mapmethod.core.model

/** A Map as defined in code: its identity, its mask as [cells] in fill order and the [flag] it takes at Completion. */
public data class MapDefinition(
    val id: MapId,
    val cells: List<Cell>,
    val flag: Flag,
) {
    init {
        require(cells.isNotEmpty()) { "Map ${id.value} has no Cells" }
        require(cells.all { (it.row >= 0) && (it.col >= 0) }) { "Map ${id.value} has Cells off its grid" }
        require(cells.distinct().size == cells.size) { "Cells of Map ${id.value} share a place on the grid" }
    }

    /** Rows of the grid the Map is drawn on, from row 0 to its southernmost Cell. */
    public val rows: Int = cells.maxOf { it.row } + 1

    /** Columns of the grid the Map is drawn on, from column 0 to its easternmost Cell. */
    public val cols: Int = cells.maxOf { it.col } + 1
}
