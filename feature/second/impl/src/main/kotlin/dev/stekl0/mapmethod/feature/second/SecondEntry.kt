package dev.stekl0.mapmethod.feature.second

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.stekl0.mapmethod.feature.second.api.SecondNavKey

public fun EntryProviderScope<NavKey>.secondEntry(onBack: () -> Unit) {
    entry<SecondNavKey>(clazzContentKey = { "second" }) {
        SecondScreen(onBack = onBack)
    }
}
