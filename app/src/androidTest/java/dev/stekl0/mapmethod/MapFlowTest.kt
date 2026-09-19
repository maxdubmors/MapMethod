package dev.stekl0.mapmethod

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Assert.assertEquals
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
