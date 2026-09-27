package dev.stekl0.mapmethod.feature.map

/** French flag band by horizontal position, west to east; derived from column at render time. */
internal enum class FlagBand {
    BLUE,
    WHITE,
    RED,
}

/** The band of [col] among [cols] columns: three equal thirds, a remainder going to the western bands. */
internal fun bandForCol(
    col: Int,
    cols: Int,
): FlagBand = FlagBand.entries[(col * FlagBand.entries.size) / cols]
