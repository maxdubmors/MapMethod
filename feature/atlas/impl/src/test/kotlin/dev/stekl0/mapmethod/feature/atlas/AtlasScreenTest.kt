package dev.stekl0.mapmethod.feature.atlas

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import dev.stekl0.mapmethod.core.data.catalogue.France
import dev.stekl0.mapmethod.core.designsystem.theme.MapMethodTheme
import dev.stekl0.mapmethod.core.model.MapId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import dev.stekl0.mapmethod.core.ui.R as UiR

/** Leafing through the Atlas: chevrons and swipes move one Map at a time, with no wrap-around. */
@RunWith(RobolectricTestRunner::class)
class AtlasScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int): String = compose.activity.getString(id)

    private fun progress(filled: Int): String =
        compose.activity.getString(UiR.string.core_ui_map_progress, filled, France.cells.size)

    private fun showAtlas(
        state: AtlasUiState = TwoMapsAtlas,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        onOpenMap: (MapId) -> Unit = {},
    ) {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MapMethodTheme { AtlasScreen(state = state, onOpenMap = onOpenMap) }
            }
        }
    }

    private fun assertShowing(filled: Int) {
        compose.onNodeWithText(progress(filled)).assertIsDisplayed()
    }

    private fun previous() = compose.onNodeWithTag("previousMapButton")

    private fun next() = compose.onNodeWithTag("nextMapButton")

    private fun pager() = compose.onNodeWithTag("atlasPager")

    @Test
    fun `the next chevron moves to the next Map`() {
        showAtlas()
        assertShowing(filled = 3)

        next().performClick()

        assertShowing(filled = 7)
    }

    @Test
    fun `the previous chevron moves back to the Map before`() {
        showAtlas()
        next().performClick()

        previous().performClick()

        assertShowing(filled = 3)
    }

    @Test
    fun `swipes move between the Maps as the chevrons do`() {
        showAtlas()

        pager().performTouchInput { swipeLeft() }
        assertShowing(filled = 7)

        pager().performTouchInput { swipeRight() }
        assertShowing(filled = 3)
    }

    @Test
    fun `the Atlas starts on the first Map with only the chevron towards the next one enabled`() {
        showAtlas()

        assertShowing(filled = 3)
        previous().assertIsDisplayed().assertIsNotEnabled()
        next().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun `on the last Map the chevron beyond it is dimmed and a swipe does not wrap around`() {
        showAtlas()
        next().performClick()

        next().assertIsDisplayed().assertIsNotEnabled()
        previous().assertIsEnabled()
        pager().performTouchInput { swipeLeft() }
        assertShowing(filled = 7)
    }

    @Test
    fun `with a single Map both chevrons are dimmed and a swipe keeps it`() {
        showAtlas(state = AtlasUiState(pages = TwoMapsAtlas.pages.take(1)))

        previous().assertIsDisplayed().assertIsNotEnabled()
        next().assertIsDisplayed().assertIsNotEnabled()
        pager().performTouchInput { swipeLeft() }
        assertShowing(filled = 3)
    }

    @Test
    fun `Open opens the Map shown`() {
        val opened = mutableListOf<MapId>()
        showAtlas(onOpenMap = opened::add)

        next().performClick()
        compose.onNodeWithTag("openMapButton").performClick()

        assertEquals(listOf(SecondMap.id), opened)
    }

    @Test
    fun `the Map shown survives the Atlas being saved and restored`() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { MapMethodTheme { AtlasScreen(state = TwoMapsAtlas, onOpenMap = {}) } }
        next().performClick()

        restoration.emulateSavedInstanceStateRestore()

        assertShowing(filled = 7)
    }

    @Test
    fun `the chevrons are labelled for screen readers`() {
        showAtlas()

        compose.onNodeWithContentDescription(string(R.string.feature_atlas_impl_previous_map)).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.feature_atlas_impl_next_map)).assertIsDisplayed()
    }

    @Test
    fun `in a right-to-left layout the chevrons swap sides and swipes follow the reading direction`() {
        showAtlas(layoutDirection = LayoutDirection.Rtl)

        assertTrue(next().getUnclippedBoundsInRoot().right < previous().getUnclippedBoundsInRoot().left)
        pager().performTouchInput { swipeRight() }
        assertShowing(filled = 7)
    }
}
