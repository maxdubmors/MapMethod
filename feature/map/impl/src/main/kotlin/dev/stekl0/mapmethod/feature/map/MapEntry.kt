package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.feature.map.api.MapNavKey
import org.orbitmvi.orbit.compose.collectAsState

public fun EntryProviderScope<NavKey>.mapEntry() {
    entry<MapNavKey> {
        val viewModel: MapViewModel = hiltViewModel()
        val state by viewModel.collectAsState()
        MapScreen(state = state, onLogCount = viewModel::logPushUps)
    }
}
