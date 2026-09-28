package dev.maxdubmors.mapmethod.feature.atlas

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.maxdubmors.mapmethod.core.data.catalogue.France
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import dev.maxdubmors.mapmethod.core.screenshottesting.captureMultiDevice
import dev.maxdubmors.mapmethod.core.screenshottesting.captureMultiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private fun franceAtlas(filledCount: Int) =
    AtlasUiState(pages = listOf(AtlasPage(MapWithProgress(France, filledCount))))

/**
 * The Atlas is static: the preview never animates, so the capture needs no clock. With France alone
 * both chevrons are dimmed; two Maps show the chevron towards the second one enabled.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class AtlasScreenScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `empty France`() {
        captureAtlas(name = "atlas_empty", state = franceAtlas(filledCount = 0))
    }

    @Test
    fun `partly filled France in pencil`() {
        captureAtlas(name = "atlas_partly_filled", state = franceAtlas(filledCount = 40))
    }

    @Test
    fun `complete France in the flag colours`() {
        captureAtlas(name = "atlas_complete", state = franceAtlas(filledCount = France.cells.size))
    }

    @Test
    fun `the first of two Maps with the chevron towards the next one enabled`() {
        captureAtlas(name = "atlas_two_maps", state = TwoMapsAtlas)
    }

    @Test
    fun `in a right-to-left layout the chevrons swap sides and point the other way`() {
        composeRule.captureMultiTheme(name = "atlas_two_maps_rtl") {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AtlasScreen(state = TwoMapsAtlas, onOpenMap = {})
            }
        }
    }

    @Test
    fun `partly filled France on every window size`() {
        val state = franceAtlas(filledCount = 40)
        composeRule.captureMultiDevice(name = "atlas_partly_filled") { AtlasScreen(state = state, onOpenMap = {}) }
    }

    @Test
    @Config(qualifiers = "+land")
    fun `the Open button stays on screen in landscape`() {
        captureAtlas(name = "atlas_landscape", state = franceAtlas(filledCount = 40))
    }

    private fun captureAtlas(name: String, state: AtlasUiState) {
        composeRule.captureMultiTheme(name = name) { AtlasScreen(state = state, onOpenMap = {}) }
    }
}
