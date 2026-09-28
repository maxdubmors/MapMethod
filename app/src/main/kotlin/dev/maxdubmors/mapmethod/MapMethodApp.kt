package dev.maxdubmors.mapmethod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.maxdubmors.mapmethod.core.navigation.rememberNavigator
import dev.maxdubmors.mapmethod.feature.atlas.api.AtlasNavKey
import dev.maxdubmors.mapmethod.feature.atlas.atlasEntry
import dev.maxdubmors.mapmethod.feature.map.api.MapNavKey
import dev.maxdubmors.mapmethod.feature.map.mapEntry
import dev.maxdubmors.mapmethod.feature.start.api.StartNavKey
import dev.maxdubmors.mapmethod.feature.start.startEntry

/**
 * Start is the entry destination and navigates one-way to the Atlas, which becomes the root: back
 * from the Atlas leaves the app. Opening a Map pushes it on top of the Atlas, and back returns there.
 * Each entry owns its ViewModel through the entry decorators.
 */
@Composable
public fun MapMethodApp(
    modifier: Modifier = Modifier,
) {
    val navigator = rememberNavigator(StartNavKey)
    val title = stringResource(R.string.app_name)
    val entryProvider =
        entryProvider {
            startEntry(title = title) { navigator.replace(AtlasNavKey) }
            atlasEntry { mapId -> navigator.navigate(MapNavKey(mapId)) }
            mapEntry(onBack = navigator::goBack)
        }

    Surface(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
            entryProvider = entryProvider,
        )
    }
}
