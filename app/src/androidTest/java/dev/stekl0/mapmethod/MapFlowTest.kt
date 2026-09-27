package dev.stekl0.mapmethod

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
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
import dev.stekl0.mapmethod.feature.map.R as MapR

class MapFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private data class Progress(val filled: Int, val total: Int)

    private fun progress(): Progress {
        val text = progressText()
        val match = checkNotNull(Regex("Filled (\\d+) of (\\d+)").find(text)) { "Unexpected progress: $text" }
        return Progress(filled = match.groupValues[1].toInt(), total = match.groupValues[2].toInt())
    }

    private fun goToMap() {
        val showMapButton = compose.onNodeWithTag("showMapButton")
        showMapButton.assertIsDisplayed()
        showMapButton.performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("mapCanvas").fetchSemanticsNodes().isNotEmpty()
        }
        // A fresh install seeds the Map off the main thread; wait for its Cells.
        compose.waitUntil(timeoutMillis = 10_000) { (progressText() == completionText()) || (progress().total > 0) }
    }

    private fun waitForFilled(filled: Int) {
        compose.waitUntil(timeoutMillis = 10_000) { progress().filled == filled }
    }

    private fun progressText(): String =
        compose.onNodeWithTag("progress")
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .first()
            .text

    private fun completionText(): String = targetContext().getString(MapR.string.feature_map_impl_complete)

    private fun waitForCompletion() {
        compose.waitUntil(timeoutMillis = 10_000) { progressText() == completionText() }
    }

    private fun entryText(): String =
        compose.onNodeWithTag("logField")
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    // The Map is drawn in the notebook palette of the system theme the app follows.
    private fun palette(): NotebookPalette {
        val nightMode = targetContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) NotebookPalette.Dark else NotebookPalette.Light
    }

    private fun mapPixels(): IntArray {
        val bitmap = compose.onNodeWithTag("mapCanvas").captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels
    }

    private fun assertMapInFlagColours() {
        compose.waitForIdle()
        val pixels = mapPixels().toSet()
        assertTrue(palette().flagWhite.toArgb() in pixels)
        assertTrue(palette().flagRed.toArgb() in pixels)
        assertFalse(palette().graphite.toArgb() in pixels)
    }

    @Test
    fun startIsEntryWithGreeting() {
        compose.onNodeWithTag("startTitle").assertIsDisplayed()
        compose.onNodeWithTag("startSubtitle").assertIsDisplayed()
        compose.onNodeWithTag("showMapButton").assertIsDisplayed()
    }

    @Test
    fun showMapNavigatesOneWayToMap() {
        goToMap()

        compose.onNodeWithTag("mapCanvas").assertIsDisplayed()
        compose.onNodeWithTag("logButton").assertIsDisplayed()
        compose.onNodeWithTag("progress").assertIsDisplayed()
    }

    @Test
    fun mapIsStartWithLogAction() {
        goToMap()

        compose.onNodeWithTag("mapCanvas").assertIsDisplayed()
        compose.onNodeWithTag("logButton").assertIsDisplayed()
        compose.onNodeWithTag("progress").assertIsDisplayed()
    }

    @Test
    fun freshMapHasNoFilledCells() {
        goToMap()

        assertEquals(0, progress().filled)
    }

    @Test
    fun logFillsExactlyOneCell() {
        goToMap()
        val total = progress().total

        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(1)

        assertEquals(Progress(filled = 1, total = total), progress())
    }

    @Test
    fun typeCountFillsThatManyCells() {
        goToMap()

        compose.onNodeWithTag("logField").performTextReplacement("5")
        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(5)
    }

    @Test
    fun stepperPlusFiveFromDefaultFillsSix() {
        goToMap()

        compose.onNodeWithTag("stepPlus5").performClick()
        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(6)
    }

    @Test
    fun inProgressMapShowsFilledCellsInGraphite() {
        goToMap()
        compose.onNodeWithTag("logField").performTextReplacement("5")
        compose.waitForIdle()
        val graphiteBefore = mapPixels().count { it == palette().graphite.toArgb() }

        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(5)
        compose.waitForIdle()

        val pixels = mapPixels()
        assertTrue(pixels.count { it == palette().graphite.toArgb() } > graphiteBefore)
        assertFalse(palette().flagRed.toArgb() in pixels)
    }

    @Test
    fun logAboveRemainingClampsAndCompletesMapInFlagColours() {
        goToMap()
        val total = progress().total

        compose.onNodeWithTag("logField").performTextReplacement("99999")
        assertEquals(total.toString(), entryText())
        compose.onNodeWithTag("logButton").performClick()
        waitForCompletion()

        compose.onNodeWithTag("logButton").assertIsNotEnabled()
        compose.onNodeWithTag("logField").assertIsNotEnabled()
        compose.onNodeWithTag("stepPlus1").assertIsNotEnabled()
        assertMapInFlagColours()
    }

    @Test
    fun reopeningCompleteMapShowsItInFlagColours() {
        goToMap()
        compose.onNodeWithTag("logField").performTextReplacement("99999")
        compose.onNodeWithTag("logButton").performClick()
        waitForCompletion()

        // Leave the app for good, then open it again from the launcher.
        compose.activityRule.scenario.close()
        ActivityScenario.launch(MainActivity::class.java).use {
            goToMap()
            waitForCompletion()

            assertMapInFlagColours()
        }
    }

    @Test
    fun clearedFieldDisablesLog() {
        goToMap()

        compose.onNodeWithTag("logField").performTextReplacement("")
        compose.onNodeWithTag("logButton").assertIsNotEnabled()
    }

    @Test
    fun rotationRetainsTypedEntry() {
        goToMap()

        compose.onNodeWithTag("logField").performTextReplacement("7")

        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        assertEquals("7", entryText())
    }

    @Test
    fun pinchZoomsAndRotationRetains() {
        goToMap()

        val canvas = compose.onNodeWithTag("mapCanvas")
        canvas.assertIsDisplayed()
        val before = canvas.captureToImage().asAndroidBitmap()

        val size = canvas.fetchSemanticsNode().size
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val start = size.width * 0.1f
        val end = size.width * 0.4f
        canvas.performTouchInput {
            down(0, Offset(centerX - start, centerY))
            down(1, Offset(centerX + start, centerY))
            repeat(6) { step ->
                val spread = start + (((end - start) * (step + 1)) / 6)
                updatePointerTo(0, Offset(centerX - spread, centerY))
                updatePointerTo(1, Offset(centerX + spread, centerY))
                move()
            }
            up(0)
            up(1)
        }
        compose.waitForIdle()
        val zoomed = canvas.captureToImage().asAndroidBitmap()
        assertFalse(zoomed.sameAs(before))

        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        compose.onNodeWithTag("mapCanvas").assertIsDisplayed()
        compose.onNodeWithTag("logButton").assertIsDisplayed()
        assertTrue(progress().total > 0)
    }

    @Test
    fun rootBackLeavesApp() {
        goToMap()

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
        compose.activity.application.registerActivityLifecycleCallbacks(callbacks)
        val application = compose.activity.application
        try {
            Espresso.pressBackUnconditionally()
            assertTrue(destroyed.await(10, TimeUnit.SECONDS))
        } finally {
            application.unregisterActivityLifecycleCallbacks(callbacks)
        }
    }
}
