package dev.stekl0.mapmethod.feature.map

/** Polish flag band by vertical position; derived from row at render time. */
internal enum class FlagBand {
    WHITE,
    RED,
}

internal fun bandForRow(
    row: Int,
    rows: Int,
): FlagBand = if (row < (rows / 2)) FlagBand.WHITE else FlagBand.RED
