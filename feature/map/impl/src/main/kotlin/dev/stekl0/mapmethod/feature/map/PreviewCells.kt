package dev.stekl0.mapmethod.feature.map

/**
 * Resolves which unfilled Cells to preview for the current Log entry.
 *
 * A null [count] (empty or invalid entry) falls back to the single next Cell
 * so the Map never loses its preview while typing; otherwise the first
 * [count] unfilled Cells in fill order are returned.
 */
internal fun previewOrderIndexes(cells: List<CellUi>, count: Int?): Set<Int> {
    val unfilled = cells.filter { !it.filled }.sortedBy { it.orderIndex }
    if (unfilled.isEmpty()) return emptySet()
    if (count == null) return setOf(unfilled.first().orderIndex)
    if (count < 1) return emptySet()
    return unfilled.take(count).map { it.orderIndex }.toSet()
}
