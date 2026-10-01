package dev.maxdubmors.mapmethod.core.data.catalogue

import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId

/**
 * A Map drawn as a [mask] of rows, `1` for a Cell, with its [flag]; Cells fill row by row north to south,
 * west to east.
 */
internal fun maskDefinition(id: MapId, mask: List<String>, flag: Flag): MapDefinition {
    val cells = mutableListOf<Cell>()
    mask.forEachIndexed { row, line ->
        line.forEachIndexed { col, mark ->
            if (mark == '1') cells.add(Cell(row = row, col = col))
        }
    }
    return MapDefinition(id = id, cells = cells, flag = flag)
}
