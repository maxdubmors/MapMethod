package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.model.Cell

/**
 * Resolves which unfilled Cells to preview for the current Log entry, of [cells] in fill order whose
 * first [filledCount] are filled.
 *
 * A null [count] (empty or invalid entry) falls back to the single next Cell
 * so the Map never loses its preview while typing; otherwise the first
 * [count] unfilled Cells in fill order are returned.
 */
internal fun previewOrderIndexes(cells: List<Cell>, filledCount: Int, count: Int?): Set<Int> {
    val unfilled = cells.drop(filledCount)
    return when {
        unfilled.isEmpty() -> emptySet()
        count == null -> setOf(unfilled.first().orderIndex)
        (count < 1) -> emptySet()
        else -> unfilled.asSequence().take(count).map { it.orderIndex }.toSet()
    }
}
