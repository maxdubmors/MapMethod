package dev.stekl0.mapmethod

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.stekl0.mapmethod.core.data.catalogue.France
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import dev.stekl0.mapmethod.core.ui.R as UiR
import dev.stekl0.mapmethod.feature.atlas.R as AtlasR
import dev.stekl0.mapmethod.feature.map.R as MapR

/** Start leads to the Atlas, the Atlas opens a Map, and back from the Map returns to a current Atlas. */
class AtlasFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val franceCells = France.cells.size

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun string(id: Int, vararg args: Any): String = targetContext().getString(id, *args)

    private fun textOf(tag: String): String =
        compose.onNodeWithTag(tag)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString(" ") { it.text }

    private fun waitForTag(tag: String) {
        compose.waitUntil(timeoutMillis = 10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun goToAtlas() {
        compose.onNodeWithTag("chooseMapButton").performClick()
        waitForTag("openMapButton")
    }

    private fun openMap() {
        compose.onNodeWithTag("openMapButton").performClick()
        waitForTag("mapCanvas")
    }

    private fun logOnMap(count: Int) {
        compose.onNodeWithTag("logField").performTextReplacement(count.toString())
        compose.onNodeWithTag("logButton").performClick()
    }

    private fun waitForAtlasProgress(filled: Int) {
        waitForTag("atlasProgress")
        val expected = string(UiR.string.core_ui_map_progress, filled, franceCells)
        compose.waitUntil(timeoutMillis = 10_000) { textOf("atlasProgress") == expected }
    }

    private fun previewPixels(): IntArray = compose.onNodeWithTag("atlasPreview").capturePixels()

    @Test
    fun choosingAMapShowsFranceInTheAtlasWithNothingFilled() {
        goToAtlas()

        assertEquals(string(UiR.string.core_ui_map_name_france), textOf("atlasName"))
        waitForAtlasProgress(filled = 0)
        compose.onNodeWithTag("atlasPreview").assertIsDisplayed()
        compose.onNodeWithTag("openMapButton").assertIsDisplayed()
    }

    @Test
    fun theBackArrowReturnsToTheAtlasShowingTheLogJustMade() {
        goToAtlas()
        openMap()
        logOnMap(1)

        compose.onNodeWithTag("backButton").performClick()

        waitForAtlasProgress(filled = 1)
    }

    @Test
    fun systemBackReturnsToTheAtlasShowingTheLogJustMade() {
        goToAtlas()
        openMap()
        logOnMap(1)
        // The Log field has the keyboard up, and the first back only closes it.
        Espresso.closeSoftKeyboard()

        Espresso.pressBack()

        waitForAtlasProgress(filled = 1)
    }

    @Test
    fun completingTheMapAndReturningShowsThePreviewInFlagColours() {
        goToAtlas()
        openMap()
        logOnMap(franceCells)

        compose.onNodeWithTag("backButton").performClick()
        waitForAtlasProgress(filled = franceCells)
        compose.waitForIdle()

        assertFranceInFlagColours(previewPixels())
        // Open works on a complete Map too, to look at the finished flag.
        openMap()
    }

    @Test
    fun aPartlyFilledPreviewIsInPencil() {
        goToAtlas()
        openMap()
        logOnMap(5)
        compose.onNodeWithTag("backButton").performClick()
        waitForAtlasProgress(filled = 5)
        compose.waitForIdle()

        assertFranceInPencil(previewPixels())
    }

    @Test
    fun theOpenButtonStaysOnScreenInLandscape() {
        goToAtlas()

        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        compose.onNodeWithTag("atlasPreview").assertIsDisplayed()
        compose.onNodeWithTag("openMapButton").assertIsDisplayed()
        openMap()
    }

    @Test
    fun theBackArrowTheOpenButtonAndThePreviewAreLabelledForScreenReaders() {
        goToAtlas()
        waitForAtlasProgress(filled = 0)
        val france = string(UiR.string.core_ui_map_name_france)

        compose.onNodeWithContentDescription(
            string(AtlasR.string.feature_atlas_impl_preview_description, france, 0, franceCells),
        ).assertIsDisplayed()
        compose.onNodeWithText(string(AtlasR.string.feature_atlas_impl_open)).assertIsDisplayed()

        openMap()
        compose.onNodeWithContentDescription(string(MapR.string.feature_map_impl_back)).assertIsDisplayed()
    }

    @Test
    fun backFromTheAtlasLeavesTheApp() {
        goToAtlas()

        val destroyed = CountDownLatch(1)
        val callbacks =
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityDestroyed(activity: Activity) {
                    if (activity is MainActivity) destroyed.countDown()
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

                override fun onActivityStarted(activity: Activity) = Unit

                override fun onActivityResumed(activity: Activity) = Unit

                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) = Unit

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            }
        val application = compose.activity.application
        application.registerActivityLifecycleCallbacks(callbacks)
        try {
            Espresso.pressBackUnconditionally()
            assertTrue(destroyed.await(10, TimeUnit.SECONDS))
        } finally {
            application.unregisterActivityLifecycleCallbacks(callbacks)
        }
    }
}
