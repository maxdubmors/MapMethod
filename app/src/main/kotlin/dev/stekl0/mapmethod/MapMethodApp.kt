package dev.stekl0.mapmethod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.stekl0.mapmethod.core.navigation.rememberNavigator
import dev.stekl0.mapmethod.feature.map.api.MapNavKey
import dev.stekl0.mapmethod.feature.map.mapEntry
import dev.stekl0.mapmethod.feature.start.api.StartNavKey
import dev.stekl0.mapmethod.feature.start.startEntry

/**
 * Start is the entry destination and navigates one-way to Map.
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
            startEntry(title = title) { navigator.replace(MapNavKey) }
            mapEntry()
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
