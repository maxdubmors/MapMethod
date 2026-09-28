package dev.stekl0.mapmethod

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        val expected = string(UiR.string.core_ui_map_progress, filled, FRANCE_CELLS)
        compose.waitUntil(timeoutMillis = 10_000) { textOf("atlasProgress") == expected }
    }

    // The Map is drawn in the notebook palette of the system theme the app follows.
    private fun palette(): NotebookPalette {
        val nightMode = targetContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) NotebookPalette.Dark else NotebookPalette.Light
    }

    private fun previewPixels(): Set<Int> {
        val bitmap = compose.onNodeWithTag("atlasPreview").captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.toSet()
    }

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
        logOnMap(FRANCE_CELLS)

        compose.onNodeWithTag("backButton").performClick()
        waitForAtlasProgress(filled = FRANCE_CELLS)
        compose.waitForIdle()

        val pixels = previewPixels()
        assertTrue(franceBlue in pixels)
        assertTrue(franceWhite in pixels)
        assertTrue(franceRed in pixels)
        assertFalse(palette().graphite.toArgb() in pixels)
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

        val pixels = previewPixels()
        assertTrue(palette().graphite.toArgb() in pixels)
        assertFalse(franceBlue in pixels)
        assertFalse(franceRed in pixels)
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
            string(AtlasR.string.feature_atlas_impl_preview_description, france, 0, FRANCE_CELLS),
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

    private companion object {
        /** Mainland France in the catalogue. */
        const val FRANCE_CELLS = 100

        // France's own blue, white and red, whatever the theme.
        val franceBlue = 0xFF0055A4.toInt()
        val franceWhite = 0xFFFFFFFF.toInt()
        val franceRed = 0xFFEF4135.toInt()
    }
}
