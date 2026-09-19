package dev.stekl0.mapmethod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.stekl0.mapmethod.core.navigation.rememberNavigator
import dev.stekl0.mapmethod.feature.second.api.SecondNavKey
import dev.stekl0.mapmethod.feature.second.secondEntry
import dev.stekl0.mapmethod.navigation.HomeNavKey
import dev.stekl0.mapmethod.ui.HomeScreen

@Composable
public fun MapMethodApp(modifier: Modifier = Modifier) {
    val navigator = rememberNavigator(HomeNavKey)
    val entryProvider =
        entryProvider<NavKey> {
            entry<HomeNavKey>(clazzContentKey = { "home" }) {
                HomeScreen(
                    onOpenSecond = { navigator.navigate(SecondNavKey) },
                )
            }
            secondEntry(onBack = navigator::goBack)
        }

    Surface(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = navigator.backStack,
            onBack = navigator::goBack,
            entryProvider = entryProvider,
        )
    }
}
