package dev.maxdubmors.mapmethod.core.data.catalogue

import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId

public val FranceMapId: MapId = MapId("france")

/**
 * Mainland France (no Corsica) as the method's paper sheet draws it: 100 Cells cropped to their own bounds,
 * 14 columns (west to east) by 13 rows (north to south).
 */
public val France: MapDefinition =
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
        flag = Flag(bands = listOf(0xFF0055A4.toInt(), 0xFFFFFFFF.toInt(), 0xFFEF4135.toInt())),
    )
