package dev.maxdubmors.mapmethod.feature.atlas

import androidx.activity.ComponentActivity
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.maxdubmors.mapmethod.core.data.catalogue.France
import dev.maxdubmors.mapmethod.core.designsystem.theme.MapMethodTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import dev.maxdubmors.mapmethod.core.ui.R as UiR

// What the window hands to composition when the system animator duration scale is 0.
private object AnimationsOff : MotionDurationScale {
    override val scaleFactor: Float = 0f
}

/** With system animations off, a chevron leafs to the next Map at once, without motion. */
@RunWith(RobolectricTestRunner::class)
class AtlasScreenWithoutAnimationsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>(effectContext = AnimationsOff)

    @Test
    fun `a chevron leafs to the next Map within a single frame`() {
        compose.setContent { MapMethodTheme { AtlasScreen(state = TwoMapsAtlas, onOpenMap = {}) } }
        compose.mainClock.autoAdvance = false

        compose.onNodeWithTag("nextMapButton").performClick()
        compose.mainClock.advanceTimeByFrame()

        val progress = compose.activity.getString(UiR.string.core_ui_map_progress, 7, France.cells.size)
        compose.onNodeWithText(progress).assertIsDisplayed()
    }
}
