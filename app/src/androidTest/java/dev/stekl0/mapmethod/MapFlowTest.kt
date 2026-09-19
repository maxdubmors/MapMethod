package dev.stekl0.mapmethod

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

public class MapFlowTest {
    @get:Rule
    public val compose = createAndroidComposeRule<MainActivity>()

    private fun progress(): Pair<Int, Int> {
        val text =
            compose.onNodeWithTag("progress")
                .fetchSemanticsNode()
                .config[SemanticsProperties.Text]
                .first()
                .text
        val match = Regex("Filled (\\d+) of (\\d+)").find(text)!!
        return match.groupValues[1].toInt() to match.groupValues[2].toInt()
    }

    @Test
    public fun mapIsStartWithLogAction() {
        compose.onNodeWithTag("mapCanvas").assertIsDisplayed()
        compose.onNodeWithTag("logButton").assertIsDisplayed()
        compose.onNodeWithTag("progress").assertIsDisplayed()
    }

    @Test
    public fun logFillsExactlyOneCell() {
        val (filledBefore, total) = progress()
        assertTrue(total > 0)

        compose.onNodeWithTag("logButton").performClick()
        compose.waitUntil(timeoutMillis = 10_000) { progress().first != filledBefore }

        val (filledAfter, totalAfter) = progress()
        assertEquals(filledBefore + 1, filledAfter)
        assertEquals(total, totalAfter)
    }

    @Test
    public fun pinchZoomsAndRotationRetains() {
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
                val spread = start + (end - start) * (step + 1) / 6
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
        val (_, total) = progress()
        assertTrue(total > 0)
    }

    @Test
    public fun rootBackLeavesApp() {
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
