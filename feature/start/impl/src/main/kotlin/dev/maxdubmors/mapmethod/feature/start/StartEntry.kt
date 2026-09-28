package dev.maxdubmors.mapmethod.feature.start

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.maxdubmors.mapmethod.feature.start.api.StartNavKey

/** The Start destination; [onChooseMap] leaves it one-way for the Atlas. */
public fun EntryProviderScope<NavKey>.startEntry(
    title: String,
    onChooseMap: () -> Unit,
) {
    entry<StartNavKey> {
        StartScreen(title = title, onChooseMap = onChooseMap)
    }
}
