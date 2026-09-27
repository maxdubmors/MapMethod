package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max

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
