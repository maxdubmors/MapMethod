package dev.stekl0.mapmethod.core.data.catalogue

import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId

public val FranceMapId: MapId = MapId("france")

/**
 * Mainland France (no Corsica) from the method's template: 100 Cells cropped to their own bounds,
 * 14 columns (west to east) by 13 rows (north to south).
 */
internal val France: MapDefinition =
    maskDefinition(
        id = FranceMapId,
        mask =
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
            ),
    )
