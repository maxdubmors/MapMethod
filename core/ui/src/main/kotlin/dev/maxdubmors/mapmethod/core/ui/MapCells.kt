package dev.maxdubmors.mapmethod.core.ui

import androidx.compose.ui.graphics.Color
import dev.maxdubmors.mapmethod.core.designsystem.theme.NotebookPalette
import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.MapWithProgress

/**
 * Marks each Cell of [map] for the notebook sheet in fill order, the same wherever a Map is drawn: filled
 * Cells are graphite while the Map is in progress and take the bands of its flag once it is complete; the
 * next [previewCount] empty Cells are outlined. The Atlas previews none.
 */
public fun mapCells(
    map: MapWithProgress,
    palette: NotebookPalette,
    previewCount: Int = 0,
): List<NotebookCell> {
    val definition = map.definition

    fun filledMark(cell: Cell): CellMark =
        if (map.isComplete) {
            CellMark.Filled(Color(definition.flag.colorAt(cell.col, definition.cols)))
        } else {
            CellMark.Filled(palette.graphite)
        }
    val preview = map.nextCells(previewCount)
    return map.filledCells.map { it.marked(filledMark(it)) } +
        preview.map { it.marked(CellMark.Preview) } +
        map.emptyCells.drop(preview.size).map { it.marked(CellMark.Empty) }
}

private fun Cell.marked(mark: CellMark) = NotebookCell(row = row, col = col, mark = mark)
