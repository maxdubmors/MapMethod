package dev.maxdubmors.mapmethod.feature.atlas

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.feature.atlas.api.AtlasNavKey
import org.orbitmvi.orbit.compose.collectAsState

/** The Atlas destination; [onOpenMap] opens the Map with that identity on top of it. */
public fun EntryProviderScope<NavKey>.atlasEntry(onOpenMap: (MapId) -> Unit) {
    entry<AtlasNavKey> {
        val viewModel = hiltViewModel<AtlasViewModel>()
        val state by viewModel.collectAsState()
        AtlasScreen(state = state, onOpenMap = onOpenMap)
    }
}
