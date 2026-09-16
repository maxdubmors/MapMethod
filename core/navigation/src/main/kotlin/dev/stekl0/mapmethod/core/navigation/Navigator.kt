package dev.stekl0.mapmethod.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Owns the single navigation history used by the navigation UI.
 */
public class Navigator internal constructor(
    private val mutableBackStack: NavBackStack<NavKey>,
) {
    /** The navigation history exposed for rendering, without mutation operations. */
    public val backStack: List<NavKey>
        get() = mutableBackStack

    /** Whether the current entry can be removed without removing the root entry. */
    public val canGoBack: Boolean
        get() = mutableBackStack.size > 1

    /** Append [key] unless it is already the current entry. */
    public fun navigate(key: NavKey) {
        if (mutableBackStack.lastOrNull() != key) {
            mutableBackStack.add(key)
        }
    }

    /** Remove the current entry when the root entry is not current. */
    public fun goBack() {
        if (canGoBack) {
            mutableBackStack.removeLastOrNull()
        }
    }
}

/**
 * Remember a single-stack [Navigator] and restore its destination keys.
 */
@Composable
public fun rememberNavigator(startKey: NavKey): Navigator {
    val backStack = rememberNavBackStack(startKey)
    return remember(backStack) { Navigator(backStack) }
}
