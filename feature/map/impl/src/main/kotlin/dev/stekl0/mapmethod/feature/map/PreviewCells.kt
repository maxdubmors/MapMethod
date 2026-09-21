package dev.stekl0.mapmethod.feature.map

/**
 * Resolves which unfilled Cells to preview for the current Log entry.
 *
 * A null [count] (empty or invalid entry) falls back to the single next Cell
 * so the Map never loses its preview while typing; otherwise the first
 * [count] unfilled Cells in fill order are returned.
 */
internal fun previewOrderIndexes(cells: List<CellUi>, count: Int?): Set<Int> {
    val unfilled = cells.asSequence().filter { !it.filled }.sortedBy { it.orderIndex }.toList()
    return when {
        unfilled.isEmpty() -> emptySet()
        count == null -> setOf(unfilled.first().orderIndex)
        (count < 1) -> emptySet()
        else -> unfilled.asSequence().take(count).map { it.orderIndex }.toSet()
    }
}
