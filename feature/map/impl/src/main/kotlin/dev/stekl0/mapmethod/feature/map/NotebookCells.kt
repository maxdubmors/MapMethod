package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.graphics.Color
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.ui.CellMark
import dev.stekl0.mapmethod.core.ui.NotebookCell

/**
 * Marks each Cell for the notebook sheet: filled Cells are graphite while the Map is in progress
 * and take the flag colours by band once it is complete; the [preview] Cells are outlined.
 */
internal fun notebookCells(
    cells: List<CellUi>,
    cols: Int,
    preview: Set<Int>,
    isComplete: Boolean,
    palette: NotebookPalette,
): List<NotebookCell> =
    cells.map { cell ->
        val mark =
            when {
                cell.filled && isComplete -> CellMark.Filled(flagColor(bandForCol(cell.col, cols), palette))
                cell.filled -> CellMark.Filled(palette.graphite)
                cell.orderIndex in preview -> CellMark.Preview
                else -> CellMark.Empty
            }
        NotebookCell(row = cell.row, col = cell.col, mark = mark)
    }

/** The confetti of Completion: small Cells in the flag colours plus graphite. */
internal fun confettiColors(palette: NotebookPalette): List<Color> =
    FlagBand.entries.map { flagColor(it, palette) } + palette.graphite

private fun flagColor(band: FlagBand, palette: NotebookPalette) =
    when (band) {
        FlagBand.BLUE -> palette.flagBlue
        FlagBand.WHITE -> palette.flagWhite
        FlagBand.RED -> palette.flagRed
    }
