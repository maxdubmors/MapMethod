package dev.stekl0.mapmethod

import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
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

    // Start leads to the Atlas, which opens France, its only Map.
    private fun goToMap() {
        val chooseMapButton = compose.onNodeWithTag("chooseMapButton")
        chooseMapButton.assertIsDisplayed()
        chooseMapButton.performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("openMapButton").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("openMapButton").performClick()
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("mapCanvas").fetchSemanticsNodes().isNotEmpty()
        }
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

    // The Log button reads "Log N push-ups"; N is the count a Log will record.
    private fun logButtonCount(): Int {
        val text =
            compose.onNodeWithTag("logButton")
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .joinToString(" ") { it.text }
        return checkNotNull(Regex("(\\d+)").find(text)) { "Unexpected Log label: $text" }.value.toInt()
    }

    private fun progressBarValue(): Float =
        compose.onNodeWithTag("progressBar")
            .fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
            .current

    private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun graphite(): Int = systemNotebookPalette().graphite.toArgb()

    private fun mapPixels(): IntArray = compose.onNodeWithTag("mapCanvas").capturePixels()

    private fun assertMapInFlagColours() {
        compose.waitForIdle()
        assertFranceInFlagColours(mapPixels())
    }

    @Test
    fun startIsEntryWithGreeting() {
        compose.onNodeWithTag("startTitle").assertIsDisplayed()
        compose.onNodeWithTag("startSubtitle").assertIsDisplayed()
        compose.onNodeWithTag("chooseMapButton").assertIsDisplayed()
    }

    @Test
    fun choosingAMapLeadsThroughTheAtlasToTheMap() {
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
    fun minusAndPlusStepTheEntryByOne() {
        goToMap()

        compose.onNodeWithTag("stepPlus1").performClick()
        compose.onNodeWithTag("stepPlus1").performClick()
        assertEquals("3", entryText())
        compose.onNodeWithTag("stepMinus1").performClick()
        assertEquals("2", entryText())
        compose.waitForIdle()
        assertEquals(2, logButtonCount())

        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(2)
    }

    @Test
    fun minusStopsAtOne() {
        goToMap()

        compose.onNodeWithTag("stepMinus1").performClick()
        assertEquals("1", entryText())
    }

    @Test
    fun chipsAddFiveAndTenAndLogFillsTheShownCount() {
        goToMap()

        compose.onNodeWithTag("chipPlus10").performClick()
        compose.onNodeWithTag("chipPlus5").performClick()
        assertEquals("16", entryText())
        compose.waitForIdle()
        val shown = logButtonCount()
        assertEquals(16, shown)

        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(shown)
    }

    @Test
    fun progressBarAndCounterSettleAfterLog() {
        goToMap()
        val total = progress().total
        assertEquals(0f, progressBarValue())

        compose.onNodeWithTag("logField").performTextReplacement("5")
        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(5)
        compose.waitForIdle()

        assertEquals(Progress(filled = 5, total = total), progress())
        assertEquals(5f / total, progressBarValue(), 1e-6f)
    }

    @Test
    fun inProgressMapShowsFilledCellsInGraphite() {
        goToMap()
        compose.onNodeWithTag("logField").performTextReplacement("5")
        compose.waitForIdle()
        val graphiteBefore = mapPixels().count { it == graphite() }

        compose.onNodeWithTag("logButton").performClick()
        waitForFilled(5)
        compose.waitForIdle()

        val pixels = mapPixels()
        assertTrue(pixels.count { it == graphite() } > graphiteBefore)
        assertFranceInPencil(pixels)
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
        compose.onNodeWithTag("stepMinus1").assertIsNotEnabled()
        compose.onNodeWithTag("stepPlus1").assertIsNotEnabled()
        compose.onNodeWithTag("chipPlus5").assertIsNotEnabled()
        compose.onNodeWithTag("chipPlus10").assertIsNotEnabled()
        assertEquals(1f, progressBarValue())
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
}
