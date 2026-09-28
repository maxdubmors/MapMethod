package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.graphics.Color
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.model.Flag
import dev.stekl0.mapmethod.core.ui.CellMark
import dev.stekl0.mapmethod.core.ui.NotebookCell

/**
 * Marks each Cell for the notebook sheet: filled Cells are graphite while the Map is in progress
 * and take the colours of the Map's [flag] by band once it is complete; the [preview] Cells are outlined.
 */
internal fun notebookCells(
    cells: List<CellUi>,
    cols: Int,
    preview: Set<Int>,
    isComplete: Boolean,
    flag: Flag,
    palette: NotebookPalette,
): List<NotebookCell> =
    cells.map { cell ->
        val mark =
            when {
                cell.filled && isComplete -> CellMark.Filled(Color(flag.colorAt(cell.col, cols)))
                cell.filled -> CellMark.Filled(palette.graphite)
                cell.orderIndex in preview -> CellMark.Preview
                else -> CellMark.Empty
            }
        NotebookCell(row = cell.row, col = cell.col, mark = mark)
    }

/** The confetti of Completion: small Cells in the colours of the Map's [flag]. */
internal fun confettiColors(flag: Flag): List<Color> = flag.bands.map { Color(it) }
