package dev.stekl0.mapmethod.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.stekl0.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette

private val GridLineWidth = 0.5.dp
private val PreviewOutlineWidth = 1.dp

// Dash and gap as fractions of a Cell side, so every side shows a few dashes at any Cell size.
@Suppress("MayBeConst")
private val PreviewDashFraction = 1f / 6f

@Suppress("MayBeConst")
private val PreviewGapFraction = 1f / 9f

/** How a country's Cell looks on the notebook sheet. */
@Immutable
public sealed interface CellMark {
    /** Not filled yet: lightly tinted. */
    public data object Empty : CellMark

    /** One of the next Cells a Log would fill: tinted, with a dashed graphite outline. */
    public data object Preview : CellMark

    /** Filled in [color]: graphite while the Map is in progress, a flag colour at Completion. */
    public data class Filled(
        val color: Color,
    ) : CellMark
}

/** A country's Cell at [row] and [col] of the grid, drawn as [mark]. */
@Immutable
public data class NotebookCell(
    val row: Int,
    val col: Int,
    val mark: CellMark,
)

/**
 * A grid of [rows] by [cols] Cells on notebook paper, centred in the canvas. The faint grid covers
 * the whole canvas, aligned with the Cells, so it keeps running to the edges under zoom and pan.
 * Only [cells] belong to the country; the rest of the grid is bare paper.
 */
@Composable
public fun NotebookCellGrid(
    rows: Int,
    cols: Int,
    cells: List<NotebookCell>,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNotebookPalette.current
    Canvas(modifier = modifier) {
        drawRect(color = palette.paper)
        if ((rows <= 0) || (cols <= 0)) return@Canvas
        val metrics = gridMetrics(size, rows, cols)
        val origin = Offset((size.width - metrics.width) / 2f, (size.height - metrics.height) / 2f)
        drawCells(cells, metrics.cell, origin, palette)
        drawGridLines(metrics.cell, origin, palette.gridLine)
        drawPreviewOutlines(cells, metrics.cell, origin, palette.previewOutline)
    }
}

private fun DrawScope.drawCells(cells: List<NotebookCell>, cellSide: Float, origin: Offset, palette: NotebookPalette) {
    val cellSize = Size(cellSide, cellSide)
    for (notebookCell in cells) {
        val color =
            when (val mark = notebookCell.mark) {
                CellMark.Empty, CellMark.Preview -> palette.countryTint
                is CellMark.Filled -> mark.color
            }
        drawRect(color = color, topLeft = cellTopLeft(notebookCell, cellSide, origin), size = cellSize)
    }
}

private fun DrawScope.drawGridLines(cellSide: Float, origin: Offset, color: Color) {
    val width = GridLineWidth.toPx()
    for (x in gridLinePositions(origin.x, cellSide, size.width)) {
        drawLine(color = color, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = width)
    }
    for (y in gridLinePositions(origin.y, cellSide, size.height)) {
        drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = width)
    }
}

private fun DrawScope.drawPreviewOutlines(cells: List<NotebookCell>, cellSide: Float, origin: Offset, color: Color) {
    val width = PreviewOutlineWidth.toPx()
    val dashes = floatArrayOf(cellSide * PreviewDashFraction, cellSide * PreviewGapFraction)
    val stroke = Stroke(width = width, pathEffect = PathEffect.dashPathEffect(dashes))
    // Inset by half the stroke so the outline stays inside its own Cell.
    val inset = width / 2f
    val outlineSize = Size(cellSide - width, cellSide - width)
    for (notebookCell in cells) {
        if (notebookCell.mark != CellMark.Preview) continue
        drawRect(
            color = color,
            topLeft = cellTopLeft(notebookCell, cellSide, origin) + Offset(inset, inset),
            size = outlineSize,
            style = stroke,
        )
    }
}

private fun cellTopLeft(notebookCell: NotebookCell, cellSide: Float, origin: Offset): Offset =
    Offset(origin.x + (notebookCell.col * cellSide), origin.y + (notebookCell.row * cellSide))
