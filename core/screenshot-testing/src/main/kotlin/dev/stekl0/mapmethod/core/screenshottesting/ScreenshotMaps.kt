package dev.stekl0.mapmethod.core.screenshottesting

import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.model.MapWithProgress

// A copy of France in the :core:data catalogue, which is internal to it; update both together.
private val FranceRows =
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

/** France as screenshots draw it: its Cells filled row by row, west to east, in its own flag. */
public val ScreenshotFrance: MapDefinition =
    MapDefinition(
        id = MapId("france"),
        cells =
            FranceRows
                .flatMapIndexed { row, line -> line.indices.filter { line[it] == '1' }.map { col -> row to col } }
                .mapIndexed { orderIndex, (row, col) -> Cell(orderIndex = orderIndex, row = row, col = col) },
        flag = Flag(bands = listOf(0xFF0055A4.toInt(), 0xFFFFFFFF.toInt(), 0xFFEF4135.toInt())),
    )

/** [ScreenshotFrance] with its first [filledCount] Cells in fill order filled. */
public fun screenshotFrance(filledCount: Int): MapWithProgress = MapWithProgress(ScreenshotFrance, filledCount)
