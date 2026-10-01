package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import dev.maxdubmors.mapmethod.core.ui.R as UiR

private val ProgressSpacing = 8.dp

@Composable
internal fun MapProgress(state: MapUiState, modifier: Modifier = Modifier) {
    val filled by animateFilledCount(state)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ProgressSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { (filled / state.map.totalCount).coerceIn(0f, 1f) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("progressBar"),
        )
        Text(
            text =
                when {
                    // Blank, not "0", until the progress is read; the line keeps its height.
                    !state.isLoaded -> {
                        ""
                    }

                    state.isComplete -> {
                        stringResource(R.string.feature_map_impl_complete)
                    }

                    else -> {
                        stringResource(UiR.string.core_ui_map_progress, filled.roundToInt(), state.map.totalCount)
                    }
                },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("progress"),
        )
    }
}

/**
 * The filled count, animated after a Log. A Map that has just loaded shows its
 * progress at once: a new animation starts from the loaded count.
 */
@Composable
private fun animateFilledCount(state: MapUiState): State<Float> {
    val filled = remember(state.isLoaded) { Animatable(state.map.filledCount.toFloat()) }
    LaunchedEffect(filled, state.map.filledCount) {
        filled.animateTo(state.map.filledCount.toFloat(), ProgressIndicatorDefaults.ProgressAnimationSpec)
    }
    return filled.asState()
}
