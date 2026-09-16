package dev.stekl0.mapmethod.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private object HomeKey : NavKey

private object SecondKey : NavKey

class NavigatorTest {
    private lateinit var backStack: NavBackStack<NavKey>
    private lateinit var navigator: Navigator

    @Before
    fun setUp() {
        backStack = NavBackStack(HomeKey)
        navigator = Navigator(backStack)
    }

    @Test
    fun startsAtHomeAndCannotGoBack() {
        assertEquals(HomeKey, backStack.last())
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun navigateAppendsSecondKey() {
        navigator.navigate(SecondKey)

        assertEquals(2, backStack.size)
        assertEquals(HomeKey, backStack[0])
        assertEquals(SecondKey, backStack[1])
        assertTrue(navigator.canGoBack)
    }

    @Test
    fun navigatingToCurrentKeyIsIgnored() {
        navigator.navigate(SecondKey)
        navigator.navigate(SecondKey)

        assertEquals(2, backStack.size)
        assertEquals(HomeKey, backStack[0])
        assertEquals(SecondKey, backStack[1])
    }

    @Test
    fun goBackRemovesCurrentKey() {
        navigator.navigate(SecondKey)

        navigator.goBack()

        assertEquals(1, backStack.size)
        assertEquals(HomeKey, backStack.last())
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun goBackAtRootIsNoOp() {
        navigator.goBack()

        assertEquals(1, backStack.size)
        assertEquals(HomeKey, backStack.last())
    }
}
