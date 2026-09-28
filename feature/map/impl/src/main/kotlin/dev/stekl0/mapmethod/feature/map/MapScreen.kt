package dev.stekl0.mapmethod.feature.map

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.stekl0.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.stekl0.mapmethod.core.ui.NotebookCellGrid
import dev.stekl0.mapmethod.core.ui.gridMetrics

private val ScreenPadding = 16.dp
private val ContentSpacing = 12.dp

// Non-const by design: const would trip standard:property-naming, PascalCase matches the dp tokens.
@Suppress("MayBeConst")
private val MinZoom = 1f

@Suppress("MayBeConst")
private val MaxZoom = 4f

@Composable
internal fun MapScreen(
    state: MapUiState,
    cascade: LogCascadeState,
    completion: CompletionState,
    onLogCount: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = state.remaining
    var entryText by rememberSaveable { mutableStateOf("1") }
    val count = if (remaining < 1) null else parseLogCount(entryText, remaining)
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(ContentSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MapCanvas(
                state = state,
                cascade = cascade,
                completion = completion,
                previewCount = count,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("mapCanvas"),
            )
            MapProgress(state = state, modifier = Modifier.fillMaxWidth())
            LogControls(
                count = count,
                remaining = remaining,
                entryText = entryText,
                onEntryTextChange = { entryText = it },
                onLogCount = onLogCount,
            )
        }
        ConfettiOverlay(completion = completion, modifier = Modifier.matchParentSize())
    }
}

@Composable
private fun MapCanvas(
    state: MapUiState,
    cascade: LogCascadeState,
    completion: CompletionState,
    previewCount: Int?,
    modifier: Modifier = Modifier,
) {
    var scale by rememberSaveable { mutableFloatStateOf(MinZoom) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(Size.Zero) }
    val cells = state.cells
    val rows = remember(cells) { (cells.maxOfOrNull { it.row } ?: -1) + 1 }
    val cols = remember(cells) { (cells.maxOfOrNull { it.col } ?: -1) + 1 }
    val palette = LocalNotebookPalette.current
    val notebookCells =
        remember(cells, cols, previewCount, state.isLoaded, state.isComplete, state.flag, palette) {
            notebookCells(
                cells = cells,
                cols = cols,
                // No Cell is next until the progress is read.
                preview = if (state.isLoaded) previewOrderIndexes(cells, previewCount) else emptySet(),
                isComplete = state.isComplete,
                flag = state.flag,
                palette = palette,
            )
        }
    val stampClock = cascade.rememberStampClock(cells)
    val recolour = completion.rememberCellRecolour(cells, palette.graphite)
    val transform =
        rememberTransformableState { _, zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(MinZoom, MaxZoom)
            val clamped =
                clampPanToGrid(
                    Offset(offsetX + panChange.x, offsetY + panChange.y),
                    scale,
                    viewport,
                    rows,
                    cols,
                )
            offsetX = clamped.x
            offsetY = clamped.y
        }
    NotebookCellGrid(
        rows = rows,
        cols = cols,
        cells = notebookCells,
        stampClock = stampClock,
        recolour = recolour,
        modifier =
            modifier
                // Outside the zoom layer, so the scaled sheet never spills over the controls.
                .clipToBounds()
                .onSizeChanged {
                    viewport = it.toSize()
                    val clamped = clampPanToGrid(Offset(offsetX, offsetY), scale, viewport, rows, cols)
                    offsetX = clamped.x
                    offsetY = clamped.y
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .transformable(transform),
    )
}

private fun clampPanToGrid(offset: Offset, scale: Float, viewport: Size, rows: Int, cols: Int): Offset {
    val content = gridMetrics(viewport, rows, cols)
    return clampZoomOffset(offset, scale, viewport, Size(content.width, content.height))
}
