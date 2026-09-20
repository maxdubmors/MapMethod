package dev.stekl0.mapmethod.feature.start

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.feature.start.api.StartNavKey

public fun EntryProviderScope<NavKey>.startEntry(
    title: String,
    onShowMap: () -> Unit,
) {
    entry<StartNavKey> {
        StartScreen(title = title, onShowMap = onShowMap)
    }
}
