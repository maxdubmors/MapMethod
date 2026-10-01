package dev.maxdubmors.mapmethod.core.model

/**
 * A Map with the user's progress on it: the first [filledCount] of its Cells in fill order are filled.
 * Progress stored before a mask lost Cells can count past the last one; such a Map is simply complete.
 */
public data class MapWithProgress(
    val definition: MapDefinition,
    val filledCount: Int,
) {
    init {
        require(filledCount >= 0) { "Map ${id.value} has $filledCount Cells filled" }
    }

    public val id: MapId get() = definition.id

    public val cells: List<Cell> get() = definition.cells

    public val totalCount: Int get() = cells.size

    /** The filled Cells, in fill order. */
    public val filledCells: List<Cell> get() = cells.take(filledCount)

    /** The empty Cells, in fill order: the ones Logs fill next. */
    public val emptyCells: List<Cell> get() = cells.drop(filledCount)

    public val emptyCount: Int get() = emptyCells.size

    /** The [count] Cells the next Log of [count] would fill, the ones previewed; fewer when fewer are empty. */
    public fun nextCells(count: Int): List<Cell> = emptyCells.take(count)

    /** Completion: every Cell is filled. */
    public val isComplete: Boolean get() = filledCount >= totalCount
}
