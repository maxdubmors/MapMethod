package dev.stekl0.mapmethod.feature.start

/** One Cell of [ItalyMotif]. */
public data class MotifCell(
    val row: Int,
    val col: Int,
    val filled: Boolean,
)

/**
 * Italy drawn as Cells, the reference motif of the launcher icon and the Start animation:
 * 16 columns (west to east) by 19 rows (north to south), mainland with Sardinia and Sicily.
 * The first [FILLED_COUNT] Cells in fill order are filled in graphite, the rest stay empty,
 * so the motif shows a Map being filled from the north.
 */
public object ItalyMotif {
    public const val FILLED_COUNT: Int = 50

    public val rows: List<String> =
        listOf(
            "....11111.......",
            ".11111111.......",
            "111111111.......",
            "11111111........",
            "11111111........",
            ".1..1111........",
            ".....1111.......",
            ".....11111......",
            "......11111.....",
            ".......1111111..",
            "........11111...",
            "..11.....111111.",
            "..11......11.111",
            "..11.......1...1",
            "..1........11...",
            "............1...",
            "........11111...",
            "........1111....",
            "..........11....",
        )

    /** Cells in fill order: row by row north to south, west to east. */
    public fun cells(): List<MotifCell> {
        val cells = mutableListOf<MotifCell>()
        rows.forEachIndexed { row, line ->
            line.forEachIndexed { col, mark ->
                if (mark == '1') {
                    cells.add(MotifCell(row = row, col = col, filled = cells.size < FILLED_COUNT))
                }
            }
        }
        return cells
    }
}
