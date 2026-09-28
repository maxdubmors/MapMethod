package dev.stekl0.mapmethod

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dev.stekl0.mapmethod.core.designsystem.theme.NotebookPalette
import dev.stekl0.mapmethod.feature.start.ItalyMotif
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Start's motif assembly, launched by each test so it can hold the clock from the first frame. */
class StartFlowTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    // The Start sheet is drawn in the notebook palette of the system theme the app follows.
    private fun palette(): NotebookPalette {
        val configuration = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration
        val nightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (nightMode == Configuration.UI_MODE_NIGHT_YES) NotebookPalette.Dark else NotebookPalette.Light
    }

    // Graphite on the Start sheet against the finished motif's filled Cells, drawn one Cell of paper in from its edge.
    private fun motifFilledFraction(): Float {
        val bitmap = compose.onNodeWithTag("startMotif").captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val cell =
            minOf(
                bitmap.width.toFloat() / (ItalyMotif.rows.first().length + 2),
                bitmap.height.toFloat() / (ItalyMotif.rows.size + 2),
            )
        val graphite = pixels.count { it == palette().graphite.toArgb() }
        return graphite / (ItalyMotif.FILLED_COUNT * cell * cell)
    }

    private fun shell(command: String) {
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }

    private fun animatorsEnabled(): Boolean {
        var enabled = false
        InstrumentationRegistry.getInstrumentation().runOnMainSync { enabled = ValueAnimator.areAnimatorsEnabled() }
        return enabled
    }

    // Flow tests run with system animations off, which skips the assembly; this one needs them on.
    private fun withAnimatorsOn(block: () -> Unit) {
        shell("settings put global animator_duration_scale 1")
        try {
            block()
        } finally {
            shell("settings put global animator_duration_scale 0")
        }
    }

    // Advances the held clock frame by frame until a node with [tag] shows, failing past [budgetMillis].
    private fun advanceUntilShown(tag: String, budgetMillis: Long) {
        var waitedMillis = 0L
        while (compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()) {
            assertTrue("$tag not shown within ${waitedMillis}ms", waitedMillis < budgetMillis)
            compose.mainClock.advanceTimeByFrame()
            waitedMillis += FRAME_MILLIS
        }
    }

    @Test
    fun motifShowsAboveTheGreetingAndIsFilledOnceAssembled() {
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.waitForIdle()

            val motifBottom = compose.onNodeWithTag("startMotif").fetchSemanticsNode().boundsInRoot.bottom
            val titleTop = compose.onNodeWithTag("startTitle").fetchSemanticsNode().boundsInRoot.top
            assertTrue(motifBottom <= titleTop)
            assertTrue(motifFilledFraction() > 0.5f)
        }
    }

    @Test
    fun chooseMapIsUsableWhileTheMotifAssembles() {
        // Frames pass only when advanced by hand, so the Start assembly holds where it is.
        compose.mainClock.autoAdvance = false
        withAnimatorsOn {
            ActivityScenario.launch(MainActivity::class.java).use {
                assertTrue("System animations are still off", animatorsEnabled())
                tapChooseMapWhileTheMotifAssembles()
            }
        }
    }

    private fun tapChooseMapWhileTheMotifAssembles() {
        advanceUntilShown("chooseMapButton", budgetMillis = NAVIGATION_BUDGET_MILLIS)
        val filledAtTap = motifFilledFraction()
        assertTrue("Motif already $filledAtTap filled at the tap", filledAtTap < 0.5f)

        compose.onNodeWithTag("chooseMapButton").assertIsEnabled().performClick()
        advanceUntilShown("atlasPreview", budgetMillis = NAVIGATION_BUDGET_MILLIS)
        compose.onNodeWithTag("openMapButton").performClick()
        advanceUntilShown("mapCanvas", budgetMillis = NAVIGATION_BUDGET_MILLIS)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithTag("mapCanvas").assertIsDisplayed()
    }

    private companion object {
        /** How soon Start must show its button, a tap on it the Atlas, and Open the Map. */
        const val NAVIGATION_BUDGET_MILLIS = 1_000L

        const val FRAME_MILLIS = 16L
    }
}
