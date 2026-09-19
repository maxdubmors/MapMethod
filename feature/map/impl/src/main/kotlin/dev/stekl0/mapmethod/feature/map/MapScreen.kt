package dev.stekl0.mapmethod.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private val ScreenPadding = 16.dp
private val ContentSpacing = 12.dp
private val CellGap = 1.dp
private val PolandWhite = Color.White
private val PolandRed = Color(0xFFDC143C)

@Composable
public fun MapScreen(
    state: MapUiState,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier,
    northernColor: Color = PolandWhite,
    southernColor: Color = PolandRed,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    nextColor: Color = MaterialTheme.colorScheme.tertiary,
) {
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
        Button(
            onClick = onLogClick,
            enabled = !state.isFull,
            modifier = Modifier.testTag("logButton"),
        ) {
            Text(text = stringResource(R.string.feature_map_impl_log_push_ups))
        }
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
    Canvas(modifier = modifier) {
        val rows = (state.cells.maxOfOrNull { it.row } ?: -1) + 1
        val cols = (state.cells.maxOfOrNull { it.col } ?: -1) + 1
        if (rows <= 0 || cols <= 0) return@Canvas
        val cell = minOf(size.width / cols, size.height / rows)
        val gap = CellGap.toPx()
        val originX = (size.width - cell * cols) / 2f
        val originY = (size.height - cell * rows) / 2f
        val byCoord = state.cells.associateBy { it.row to it.col }
        for (row in 0 until rows) {
            val filledColor =
                if (bandForRow(row, rows) == FlagBand.WHITE) northernColor else southernColor
            for (col in 0 until cols) {
                val cellUi = byCoord[row to col] ?: continue
                val topLeft = Offset(originX + col * cell + gap / 2f, originY + row * cell + gap / 2f)
                val cellSize = Size(cell - gap, cell - gap)
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
}
