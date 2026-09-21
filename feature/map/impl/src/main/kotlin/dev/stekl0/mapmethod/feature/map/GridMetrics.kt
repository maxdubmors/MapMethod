package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max

/** Pixel geometry of the grid inside a viewport of the given size. */
internal data class GridMetrics(
    val cell: Float,
    val width: Float,
    val height: Float,
)

internal fun gridMetrics(viewport: Size, rows: Int, cols: Int): GridMetrics {
    if ((rows <= 0) || (cols <= 0)) return GridMetrics(cell = 0f, width = 0f, height = 0f)
    val cell = minOf(viewport.width / cols, viewport.height / rows)
    return GridMetrics(cell = cell, width = cell * cols, height = cell * rows)
}

/** Clamps a pan offset symmetrically so scaled content cannot leave empty viewport edges. */
internal fun clampZoomOffset(
    offset: Offset,
    scale: Float,
    viewport: Size,
    content: Size,
): Offset {
    fun clampAxis(position: Float, viewportExtent: Float, contentExtent: Float): Float {
        val range = max((((contentExtent * scale) - viewportExtent) / 2f), 0f)
        if (range == 0f) return 0f
        return position.coerceIn(-range, range)
    }

    return Offset(
        clampAxis(offset.x, viewport.width, content.width),
        clampAxis(offset.y, viewport.height, content.height),
    )
}
