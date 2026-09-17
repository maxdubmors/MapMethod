package dev.stekl0.mapmethod

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.stekl0.mapmethod.core.navigation.rememberNavigator
import dev.stekl0.mapmethod.navigation.HomeNavKey
import dev.stekl0.mapmethod.navigation.SecondNavKey
import dev.stekl0.mapmethod.ui.HomeScreen
import dev.stekl0.mapmethod.ui.SecondScreen

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
            entry<SecondNavKey>(clazzContentKey = { "second" }) {
                SecondScreen(onBack = navigator::goBack)
            }
        }

    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier,
        onBack = navigator::goBack,
        entryProvider = entryProvider,
    )
}
