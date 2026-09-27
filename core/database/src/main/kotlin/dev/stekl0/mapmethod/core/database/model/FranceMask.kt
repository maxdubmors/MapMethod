package dev.stekl0.mapmethod.core.database.model

/**
 * Boolean mask of mainland France (no Corsica) from the method's template: 100 Cells cropped to
 * their own bounds, 14 columns (west to east) by 13 rows (north to south).
 */
internal object FranceMask {
    val rows: List<String> =
        listOf(
            ".......1......",
            ".......111....",
            "...1.111111...",
            "11111111111111",
            "1111111111111.",
            "..1111111111..",
            "..111111111...",
            "...111111111..",
            "...111111111..",
            "...1111111111.",
            "...111111111..",
            "....1111......",
            "......11......",
        )

    /** Cells in fill order: row by row north to south, west to east. */
    fun entities(): List<CellEntity> {
        val entities = mutableListOf<CellEntity>()
        var order = 0
        rows.forEachIndexed { row, line ->
            line.forEachIndexed { col, mark ->
                if (mark == '1') {
                    entities.add(CellEntity(orderIndex = order++, row = row, col = col, filledAt = null))
                }
            }
        }
        return entities
    }
}
