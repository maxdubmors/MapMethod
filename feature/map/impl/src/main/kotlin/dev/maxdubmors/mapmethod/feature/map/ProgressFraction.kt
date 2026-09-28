package dev.maxdubmors.mapmethod.feature.map

/** Share of the Map's [total] Cells that are [filled], within 0..1; 0 while the Map is not loaded. */
internal fun progressFraction(filled: Float, total: Int): Float =
    if (total < 1) 0f else (filled / total).coerceIn(0f, 1f)
