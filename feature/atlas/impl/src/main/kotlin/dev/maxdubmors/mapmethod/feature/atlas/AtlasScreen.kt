package dev.maxdubmors.mapmethod.feature.atlas

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.maxdubmors.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.ui.NotebookCellGrid
import dev.maxdubmors.mapmethod.core.ui.mapCells
import kotlinx.coroutines.launch
import dev.maxdubmors.mapmethod.core.ui.R as UiR

private val ScreenPadding = 24.dp
private val ContentSpacing = 16.dp
private val PreviewMaxWidth = 480.dp

// An icon button's touch target.
private val ChevronWidth = 48.dp

// The chevrons sit beside the widest preview, not at the far edges of a wide window.
private val PagerMaxWidth = PreviewMaxWidth + ChevronWidth * 2

// A chevron with no Map beyond it: the graphite faded, like a line half rubbed out.
@Suppress("MayBeConst")
private val DimmedAlpha = 0.38f

/**
 * The Atlas, one Map at a time: the country's name, a preview of its Map flanked by chevrons, how
 * far along it is and the Open button. Chevrons and swipes leaf through the Maps in catalogue
 * order, with no wrap-around; the page shown is saved, so it survives rotation and returning from a
 * Map.
 */
@Composable
internal fun AtlasScreen(
    state: AtlasUiState,
    onOpenMap: (MapId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.pages.isEmpty()) return@Column
        // Created once the Maps are known, so a saved page is never restored into an empty pager.
        val pagerState = rememberPagerState { state.pages.size }
        // Takes what the button leaves, so the button stays on screen in landscape too.
        AtlasPager(state = state, pagerState = pagerState, modifier = Modifier.weight(1f))
        Button(
            onClick = { onOpenMap(state.pages[pagerState.currentPage].id) },
            modifier = Modifier.testTag("openMapButton"),
        ) {
            Text(text = stringResource(R.string.feature_atlas_impl_open))
        }
    }
}

/** The pages between the two chevrons; a chevron moves one Map towards its side, or is dimmed at the edge. */
@Composable
private fun AtlasPager(
    state: AtlasUiState,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    // Steps from the page being moved to, so a second tap during the motion moves on once more.
    fun leaf(by: Int) {
        scope.launch { pagerState.animateScrollToPage(pagerState.targetPage + by) }
    }
    Row(
        modifier = modifier.widthIn(max = PagerMaxWidth).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chevron(
            iconRes = R.drawable.feature_atlas_impl_ic_previous,
            labelRes = R.string.feature_atlas_impl_previous_map,
            enabled = pagerState.currentPage > 0,
            onClick = { leaf(by = -1) },
            modifier = Modifier.testTag("previousMapButton"),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).testTag("atlasPager"),
        ) { index ->
            AtlasPageContent(page = state.pages[index], modifier = Modifier.fillMaxSize())
        }
        Chevron(
            iconRes = R.drawable.feature_atlas_impl_ic_next,
            labelRes = R.string.feature_atlas_impl_next_map,
            enabled = pagerState.currentPage < state.pages.lastIndex,
            onClick = { leaf(by = 1) },
            modifier = Modifier.testTag("nextMapButton"),
        )
    }
}

// Auto-mirrored, so each chevron points along the reading direction; graphite like the back arrow.
@Composable
private fun Chevron(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val graphite = LocalNotebookPalette.current.graphite
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors =
            IconButtonDefaults.iconButtonColors(
                contentColor = graphite,
                disabledContentColor = graphite.copy(alpha = DimmedAlpha),
            ),
        modifier = modifier,
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = stringResource(labelRes))
    }
}

/** One Map: its name, its preview and how far along it is. */
@Composable
private fun AtlasPageContent(
    page: AtlasPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val name = stringResource(page.nameRes)
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("atlasName"),
        )
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
    val description =
        stringResource(R.string.feature_atlas_impl_preview_description, name, page.filledCount, page.totalCount)
    NotebookCellGrid(
        rows = page.map.definition.rows,
        cols = page.map.definition.cols,
        cells = cells,
        modifier = modifier.semantics { contentDescription = description },
    )
}
