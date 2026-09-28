package dev.maxdubmors.mapmethod.core.model

/** A Map with the user's progress on it: the first [filledCount] of its Cells in fill order are filled. */
public data class MapWithProgress(
    val definition: MapDefinition,
    val filledCount: Int,
) {
    public val id: MapId get() = definition.id

    public val cells: List<Cell> get() = definition.cells

    public val totalCount: Int get() = cells.size

    /** The Cell the next Log fills first; null once the Map is complete. */
    public val nextCell: Cell? get() = cells.getOrNull(filledCount)

    /** Completion: every Cell is filled. */
    public val isComplete: Boolean get() = filledCount >= totalCount
}
