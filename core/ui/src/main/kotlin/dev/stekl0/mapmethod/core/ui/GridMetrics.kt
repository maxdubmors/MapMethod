package dev.stekl0.mapmethod.core.ui

import androidx.compose.ui.geometry.Size
import kotlin.math.floor

/** Pixel geometry of a Cell grid inside a viewport of the given size. */
public data class GridMetrics(
    val cell: Float,
    val width: Float,
    val height: Float,
)

public fun gridMetrics(viewport: Size, rows: Int, cols: Int): GridMetrics {
    if ((rows <= 0) || (cols <= 0)) return GridMetrics(cell = 0f, width = 0f, height = 0f)
    val cell = minOf(viewport.width / cols, viewport.height / rows)
    return GridMetrics(cell = cell, width = cell * cols, height = cell * rows)
}

/**
 * Positions of the grid lines along one axis: aligned with the Cell edges that start at [origin],
 * one every [cellSide] across the whole 0-through-[extent] range, so the grid runs past the Cells to the edges.
 */
internal fun gridLinePositions(origin: Float, cellSide: Float, extent: Float): List<Float> {
    if (cellSide <= 0f) return emptyList()
    val first = origin - (floor(origin / cellSide) * cellSide)
    return generateSequence(0) { it + 1 }
        .map { index -> first + (index * cellSide) }
        .takeWhile { position -> position <= extent }
        .toList()
}
