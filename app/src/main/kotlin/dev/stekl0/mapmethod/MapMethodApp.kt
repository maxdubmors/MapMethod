package dev.stekl0.mapmethod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.stekl0.mapmethod.core.navigation.rememberNavigator
import dev.stekl0.mapmethod.feature.map.MapScreen
import dev.stekl0.mapmethod.feature.map.MapViewModel
import dev.stekl0.mapmethod.feature.map.api.MapNavKey
import dev.stekl0.mapmethod.feature.map.mapEntry
import org.orbitmvi.orbit.compose.collectAsState

/**
 * Map is the start and only destination in v1. The [MapViewModel] is scoped
 * to the activity: Navigation 3 exposes no entry ViewModelStoreOwner, and a
 * single destination makes activity scope equal screen lifetime. Entry
 * scoping arrives with the second destination.
 */
@Composable
public fun MapMethodApp(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val navigator = rememberNavigator(MapNavKey)
    val entryProvider =
        entryProvider<NavKey> {
            mapEntry {
                MapScreen(state = state, onLogCount = viewModel::logPushUps)
            }
        }

    Surface(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            entryProvider = entryProvider,
        )
    }
}
