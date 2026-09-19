package dev.stekl0.mapmethod.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

private val ScreenPadding = 16.dp
private val ContentSpacing = 12.dp
private val StepperSpacing = 4.dp
private val CellGap = 1.dp
private val PolandWhite = Color.White
private val PolandRed = Color(0xFFDC143C)

@Composable
public fun MapScreen(
    state: MapUiState,
    onLogCount: (Int) -> Unit,
    modifier: Modifier = Modifier,
    northernColor: Color = PolandWhite,
    southernColor: Color = PolandRed,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    nextColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val remaining = state.totalCount - state.filledCount
    var entryText by rememberSaveable { mutableStateOf("1") }
    val count = if (state.isFull) null else parseLogCount(entryText, remaining)
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MapCanvas(
            state = state,
            northernColor = northernColor,
            southernColor = southernColor,
            emptyColor = emptyColor,
            nextColor = nextColor,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("mapCanvas"),
        )
        Text(
            text = stringResource(R.string.feature_map_impl_progress, state.filledCount, state.totalCount),
            modifier = Modifier.testTag("progress"),
        )
        LogControls(
            count = count,
            remaining = remaining,
            entryText = entryText,
            onEntryTextChange = { entryText = it },
            onLogCount = onLogCount,
        )
    }
}

@Composable
private fun LogControls(
    count: Int?,
    remaining: Int,
    entryText: String,
    onEntryTextChange: (String) -> Unit,
    onLogCount: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = entryText,
            onValueChange = { typed ->
                if (typed.all(Char::isDigit)) onEntryTextChange(clampEntryText(typed, entryText, remaining))
            },
            label = { Text(text = stringResource(R.string.feature_map_impl_log_count_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            enabled = remaining > 0,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("logField"),
        )
        StepperRow(
            count = count,
            remaining = remaining,
            enabled = remaining > 0,
            onStep = { onEntryTextChange(it.toString()) },
        )
        Button(
            onClick = { count?.let(onLogCount) },
            enabled = count != null,
            modifier = Modifier.testTag("logButton"),
        ) {
            Text(
                text =
                    if (count != null) {
                        stringResource(R.string.feature_map_impl_log_push_ups, count)
                    } else {
                        stringResource(R.string.feature_map_impl_log_push_ups_empty)
                    },
            )
        }
    }
}

private val StepSizes = listOf(-10, -5, -1, 1, 5, 10)

@Composable
private fun StepperRow(count: Int?, remaining: Int, enabled: Boolean, onStep: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(StepperSpacing, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (step in StepSizes) {
            StepperButton(step = step, current = count, remaining = remaining, enabled = enabled, onStep = onStep)
        }
    }
}

@Composable
private fun StepperButton(step: Int, current: Int?, remaining: Int, enabled: Boolean, onStep: (Int) -> Unit) {
    val label = if (step > 0) "+$step" else step.toString()
    val tag = if (step > 0) "stepPlus$step" else "stepMinus${-step}"
    OutlinedButton(
        onClick = { onStep(stepLogCount(current = current, step = step, remaining = remaining)) },
        enabled = enabled,
        modifier = Modifier.testTag(tag),
    ) {
        Text(text = label)
    }
}

@Composable
private fun MapCanvas(
    state: MapUiState,
    northernColor: Color,
    southernColor: Color,
    emptyColor: Color,
    nextColor: Color,
    modifier: Modifier = Modifier,
) {
    val minZoom = 1f
    val maxZoom = 4f
    var scale by rememberSaveable { mutableFloatStateOf(minZoom) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(Size.Zero) }
    val rows = (state.cells.maxOfOrNull { it.row } ?: -1) + 1
    val cols = (state.cells.maxOfOrNull { it.col } ?: -1) + 1
    val transform =
        rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(minZoom, maxZoom)
            val content = gridMetrics(viewport, rows, cols)
            val clamped =
                clampZoomOffset(
                    Offset(offsetX + panChange.x, offsetY + panChange.y),
                    scale,
                    viewport,
                    Size(content.width, content.height),
                )
            offsetX = clamped.x
            offsetY = clamped.y
        }
    Canvas(
        modifier =
            modifier
                .onSizeChanged {
                    viewport = it.toSize()
                    val content = gridMetrics(viewport, rows, cols)
                    val clamped =
                        clampZoomOffset(
                            Offset(offsetX, offsetY),
                            scale,
                            viewport,
                            Size(content.width, content.height),
                        )
                    offsetX = clamped.x
                    offsetY = clamped.y
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
                .transformable(transform)
                .clipToBounds(),
    ) {
        drawMapGrid(
            cells = state.cells,
            rows = rows,
            cols = cols,
            northernColor = northernColor,
            southernColor = southernColor,
            emptyColor = emptyColor,
            nextColor = nextColor,
        )
    }
}

private fun DrawScope.drawMapGrid(
    cells: List<CellUi>,
    rows: Int,
    cols: Int,
    northernColor: Color,
    southernColor: Color,
    emptyColor: Color,
    nextColor: Color,
) {
    if (rows <= 0 || cols <= 0) return
    val metrics = gridMetrics(size, rows, cols)
    val gap = CellGap.toPx()
    val originX = (size.width - metrics.width) / 2f
    val originY = (size.height - metrics.height) / 2f
    val byCoord = cells.associateBy { it.row to it.col }
    for (row in 0 until rows) {
        val filledColor =
            if (bandForRow(row, rows) == FlagBand.WHITE) northernColor else southernColor
        for (col in 0 until cols) {
            val cellUi = byCoord[row to col] ?: continue
            val topLeft =
                Offset(
                    originX + col * metrics.cell + gap / 2f,
                    originY + row * metrics.cell + gap / 2f,
                )
            val cellSize = Size(metrics.cell - gap, metrics.cell - gap)
            drawRect(
                color = if (cellUi.filled) filledColor else emptyColor,
                topLeft = topLeft,
                size = cellSize,
            )
            if (cellUi.isNext && !cellUi.filled) {
                drawRect(color = nextColor, topLeft = topLeft, size = cellSize, style = Stroke(width = gap))
            }
        }
    }
}
