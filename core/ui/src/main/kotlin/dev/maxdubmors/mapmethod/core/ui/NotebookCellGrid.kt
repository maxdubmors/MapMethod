package dev.maxdubmors.mapmethod.core.ui

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
import dev.maxdubmors.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.maxdubmors.mapmethod.core.designsystem.theme.NotebookPalette

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

/**
 * Changes the colour filled Cells are drawn in while it lasts. Read while drawing, like [StampClock],
 * so a colour that changes every frame never recomposes the grid.
 */
public fun interface CellRecolour {
    /** The colour to draw the filled Cell at [cellIndex] of the grid's cells in, instead of its mark's [color]. */
    public fun colorOf(cellIndex: Int, color: Color): Color
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
 * Only [cells] belong to the country; the rest of the grid is bare paper. Filled Cells stamp in as
 * [stampClock] says; without one they are all drawn stamped. A [recolour] overrides the colour of
 * filled Cells.
 */
@Composable
public fun NotebookCellGrid(
    rows: Int,
    cols: Int,
    cells: List<NotebookCell>,
    modifier: Modifier = Modifier,
    stampClock: StampClock? = null,
    recolour: CellRecolour? = null,
) {
    val palette = LocalNotebookPalette.current
    Canvas(modifier = modifier) {
        drawRect(color = palette.paper)
        if ((rows <= 0) || (cols <= 0)) return@Canvas
        val metrics = gridMetrics(size, rows, cols)
        val origin = metrics.originIn(size)
        drawCells(cells, metrics.cell, origin, palette, stampClock, recolour)
        drawGridLines(metrics.cell, origin, palette.gridLine)
        drawPreviewOutlines(cells, metrics.cell, origin, palette.previewOutline)
    }
}

private fun DrawScope.drawCells(
    cells: List<NotebookCell>,
    cellSide: Float,
    origin: Offset,
    palette: NotebookPalette,
    stampClock: StampClock?,
    recolour: CellRecolour?,
) {
    val cellSize = Size(cellSide, cellSide)
    cells.forEachIndexed { index, notebookCell ->
        val topLeft = cellTopLeft(notebookCell, cellSide, origin)
        val mark = notebookCell.mark
        val playTime = if (mark is CellMark.Filled) stampClock?.playTimeMillis(index) ?: CellStampMillis else 0L
        when {
            mark !is CellMark.Filled -> {
                drawRect(color = palette.countryTint, topLeft = topLeft, size = cellSize)
            }

            playTime >= CellStampMillis -> {
                drawRect(color = filledColor(mark, index, recolour), topLeft = topLeft, size = cellSize)
            }

            else -> {
                drawRect(color = palette.countryTint, topLeft = topLeft, size = cellSize)
                drawStamp(filledColor(mark, index, recolour), topLeft, cellSide, playTime)
            }
        }
    }
}

private fun filledColor(mark: CellMark.Filled, cellIndex: Int, recolour: CellRecolour?): Color =
    recolour?.colorOf(cellIndex, mark.color) ?: mark.color

// The fill grows from the Cell's centre; at the overshoot it slightly passes the Cell's edge, like a pencil stroke.
private fun DrawScope.drawStamp(color: Color, cellTopLeft: Offset, cellSide: Float, playTimeMillis: Long) {
    val alpha = stampAlpha(playTimeMillis)
    if (alpha <= 0f) return
    val side = cellSide * stampScale(playTimeMillis)
    val inset = (cellSide - side) / 2f
    drawRect(color = color, topLeft = cellTopLeft + Offset(inset, inset), size = Size(side, side), alpha = alpha)
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
