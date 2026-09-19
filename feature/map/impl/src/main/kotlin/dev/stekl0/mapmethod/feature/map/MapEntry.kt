package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.feature.map.api.MapNavKey

public fun EntryProviderScope<NavKey>.mapEntry(screen: @Composable () -> Unit) {
    entry<MapNavKey>(clazzContentKey = { "map" }) {
        screen()
    }
}
