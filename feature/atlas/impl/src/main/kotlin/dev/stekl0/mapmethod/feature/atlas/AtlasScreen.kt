package dev.stekl0.mapmethod.feature.atlas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.stekl0.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.stekl0.mapmethod.core.model.MapId
import dev.stekl0.mapmethod.core.ui.NotebookCellGrid
import dev.stekl0.mapmethod.core.ui.mapCells
import dev.stekl0.mapmethod.core.ui.R as UiR

private val ScreenPadding = 24.dp
private val ContentSpacing = 16.dp
private val PreviewMaxWidth = 480.dp

/**
 * The Atlas, top to bottom: the country's name, a preview of its Map, how far along it is and the
 * Open button. Leafing between Maps comes later; for now the Atlas shows its first Map.
 */
@Composable
internal fun AtlasScreen(
    state: AtlasUiState,
    onOpenMap: (MapId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val page = state.pages.firstOrNull()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (page == null) return@Column
        val name = stringResource(page.nameRes)
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("atlasName"),
        )
        // Takes what the texts and the button leave, so the button stays on screen in landscape too.
        AtlasPreview(
            page = page,
            name = name,
            modifier =
                Modifier
                    .weight(1f)
                    .widthIn(max = PreviewMaxWidth)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .testTag("atlasPreview"),
        )
        Text(
            text = stringResource(UiR.string.core_ui_map_progress, page.filledCount, page.totalCount),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("atlasProgress"),
        )
        Button(
            onClick = { onOpenMap(page.id) },
            modifier = Modifier.testTag("openMapButton"),
        ) {
            Text(text = stringResource(R.string.feature_atlas_impl_open))
        }
    }
}

/** The Map as it stands, in pencil or in its flag colours: a still snapshot with no next Cell outlined. */
@Composable
private fun AtlasPreview(
    page: AtlasPage,
    name: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalNotebookPalette.current
    val cells = remember(page.map, palette) { mapCells(page.map, palette) }
    val rows = remember(page.map) { (page.map.cells.maxOfOrNull { it.row } ?: -1) + 1 }
    val cols = remember(page.map) { (page.map.cells.maxOfOrNull { it.col } ?: -1) + 1 }
    val description =
        stringResource(R.string.feature_atlas_impl_preview_description, name, page.filledCount, page.totalCount)
    NotebookCellGrid(
        rows = rows,
        cols = cols,
        cells = cells,
        modifier = modifier.semantics { contentDescription = description },
    )
}
