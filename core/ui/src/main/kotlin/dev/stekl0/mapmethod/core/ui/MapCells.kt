package dev.stekl0.mapmethod.core.ui

import androidx.compose.ui.graphics.Color
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.model.MapWithProgress

/**
 * Marks each Cell of [map] for the notebook sheet, the same wherever a Map is drawn: filled Cells are
 * graphite while the Map is in progress and take the bands of its flag once it is complete; the empty
 * Cells whose fill order index is in [preview] are outlined. The Atlas previews none.
 */
public fun mapCells(
    map: MapWithProgress,
    palette: NotebookPalette,
    preview: Set<Int> = emptySet(),
): List<NotebookCell> {
    val cells = map.cells
    val cols = (cells.maxOfOrNull { it.col } ?: -1) + 1
    val flag = map.definition.flag
    return cells.mapIndexed { position, cell ->
        val filled = position < map.filledCount
        val mark =
            when {
                filled && map.isComplete -> CellMark.Filled(Color(flag.colorAt(cell.col, cols)))
                filled -> CellMark.Filled(palette.graphite)
                cell.orderIndex in preview -> CellMark.Preview
                else -> CellMark.Empty
            }
        NotebookCell(row = cell.row, col = cell.col, mark = mark)
    }
}
