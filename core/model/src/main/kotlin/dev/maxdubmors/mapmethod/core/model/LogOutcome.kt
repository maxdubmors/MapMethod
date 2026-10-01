package dev.maxdubmors.mapmethod.core.model

/** What a Log did to a Map: its progress [before] the Log and [after] it; the same when the Log filled nothing. */
public data class LogOutcome(
    val before: MapWithProgress,
    val after: MapWithProgress,
) {
    /** The Cells the Log newly filled, in fill order. */
    public val filledCells: List<Cell> get() = after.cells.subList(before.filledCount, after.filledCount)

    /** Completion: the Log filled the Map's last Cell. */
    public val completesMap: Boolean get() = !before.isComplete && after.isComplete
}
