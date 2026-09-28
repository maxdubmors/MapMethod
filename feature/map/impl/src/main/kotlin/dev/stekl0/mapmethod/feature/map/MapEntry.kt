package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.feature.map.api.MapNavKey
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

public fun EntryProviderScope<NavKey>.mapEntry() {
    entry<MapNavKey> { key ->
        val viewModel = hiltViewModel<MapViewModel, MapViewModel.Factory> { factory -> factory.create(key) }
        val state by viewModel.collectAsState()
        val cascade = rememberLogCascadeState(state)
        val completion = rememberCompletionState(state)
        viewModel.collectSideEffect { event ->
            when (event) {
                is MapEvent.LogFilled -> cascade.play(event.orderIndexes)
                MapEvent.Completion -> completion.play(cascade)
            }
        }
        MapScreen(state = state, cascade = cascade, completion = completion, onLogCount = viewModel::logPushUps)
    }
}
