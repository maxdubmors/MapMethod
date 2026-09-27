package dev.stekl0.mapmethod.feature.start

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import dev.stekl0.mapmethod.core.designsystem.motion.isReducedMotion
import dev.stekl0.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.core.ui.CellMark
import dev.stekl0.mapmethod.core.ui.CellStampMillis
import dev.stekl0.mapmethod.core.ui.NotebookCell
import dev.stekl0.mapmethod.core.ui.NotebookCellGrid
import dev.stekl0.mapmethod.core.ui.StampClock
import dev.stekl0.mapmethod.core.ui.cellTopLeft
import dev.stekl0.mapmethod.core.ui.gridMetrics

// Bare paper around the motif, in Cells, so the country never touches the sheet's edge.
@Suppress("MayBeConst")
private val MarginCells = 1

// Far enough in the past that every Cell of an assembly waiting to start is still hidden.
@Suppress("MayBeConst")
private val NotStarted = Long.MIN_VALUE / 2

/** Rows of the sheet the motif is drawn on, its paper margin included. */
internal val MotifSheetRows: Int = ItalyMotif.rows.size + (2 * MarginCells)

/** Columns of the sheet the motif is drawn on, its paper margin included. */
internal val MotifSheetCols: Int = ItalyMotif.rows.first().length + (2 * MarginCells)

/**
 * Marks the motif's Cells for the notebook sheet, in fill order: filled Cells are graphite, the rest
 * stay tinted, and with [showPreview] the next Cell in fill order is outlined.
 */
internal fun motifSheetCells(showPreview: Boolean, palette: NotebookPalette): List<NotebookCell> {
    val motif = ItalyMotif.cells()
    val next = if (showPreview) motif.indexOfFirst { !it.filled } else -1
    return motif.mapIndexed { index, cell ->
        val mark =
            when {
                cell.filled -> CellMark.Filled(palette.graphite)
                index == next -> CellMark.Preview
                else -> CellMark.Empty
            }
        NotebookCell(row = cell.row + MarginCells, col = cell.col + MarginCells, mark = mark)
    }
}

/**
 * The Italy motif of the launcher icon on grid paper. Once per entry its filled Cells stamp in one
 * by one, then the next preview Cell breathes. With system animations off the finished motif shows
 * at once, still.
 */
@Composable
internal fun MotifSheet(modifier: Modifier = Modifier) {
    val palette = LocalNotebookPalette.current
    // Saved, so the assembly plays once per entry and not again after rotation.
    var played by rememberSaveable { mutableStateOf(false) }
    var assembling by remember { mutableStateOf(!played && !isReducedMotion()) }
    var breathing by remember { mutableStateOf(false) }
    // Read only while drawing, so each frame redraws the sheet without recomposing it.
    var elapsedMillis by remember { mutableLongStateOf(NotStarted) }
    var breathMillis by remember { mutableLongStateOf(0L) }
    val starts = remember { assemblySchedule(ItalyMotif.FILLED_COUNT) }
    val cells = remember(assembling, palette) { motifSheetCells(showPreview = !assembling, palette = palette) }
    val stampClock =
        remember(assembling, starts) {
            if (!assembling) return@remember null
            // The filled Cells come first in fill order, so a grid index is also a schedule position.
            val startArray = starts.toLongArray()
            StampClock { index -> if (index < startArray.size) elapsedMillis - startArray[index] else CellStampMillis }
        }
    LaunchedEffect(Unit) {
        // Also when the assembly is skipped, so it never plays later in this entry.
        played = true
        if (assembling) {
            val duration = starts.assemblyDurationMillis()
            val start = withFrameMillis { it }
            var elapsed = 0L
            while (elapsed < duration) {
                elapsedMillis = elapsed
                elapsed = withFrameMillis { it - start }
            }
            assembling = false
        }
        if (isReducedMotion()) return@LaunchedEffect
        breathing = true
        // Endless, so it runs as an infinite animation that tests do not wait for.
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) {
            breathMillis = withInfiniteAnimationFrameMillis { it - start }
        }
    }
    Box(modifier = modifier) {
        NotebookCellGrid(
            rows = MotifSheetRows,
            cols = MotifSheetCols,
            cells = cells,
            stampClock = stampClock,
            modifier = Modifier.matchParentSize(),
        )
        if (breathing) {
            PreviewBreath(
                color = palette.graphite,
                alpha = { breathAlpha(breathMillis) },
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

// A faint graphite fill over the next preview Cell. Only its layer's alpha changes each frame,
// so breathing neither redraws the sheet nor this fill.
@Composable
private fun PreviewBreath(
    color: Color,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    val next = remember { ItalyMotif.cells().first { !it.filled } }
    Canvas(modifier = modifier.graphicsLayer { this.alpha = alpha() }) {
        val metrics = gridMetrics(size, MotifSheetRows, MotifSheetCols)
        drawRect(
            color = color,
            topLeft = metrics.cellTopLeft(size, row = next.row + MarginCells, col = next.col + MarginCells),
            size = Size(metrics.cell, metrics.cell),
        )
    }
}
