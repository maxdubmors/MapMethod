package dev.maxdubmors.mapmethod.feature.start

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.maxdubmors.mapmethod.core.screenshottesting.captureMultiDevice
import dev.maxdubmors.mapmethod.core.screenshottesting.captureMultiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Just past the last stamp: the motif is assembled and the preview Cell's breath has barely begun.
private val SettledMillis = assemblySchedule(ItalyMotif.FILLED_COUNT).assemblyDurationMillis() + 100L

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class StartScreenScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `Start with the motif assembled`() {
        composeRule.captureMultiTheme(name = "start", settleMillis = SettledMillis) {
            StartScreen(title = "MapMethod", onChooseMap = {})
        }
    }

    @Test
    fun `Start on every window size`() {
        composeRule.captureMultiDevice(name = "start", settleMillis = SettledMillis) {
            StartScreen(title = "MapMethod", onChooseMap = {})
        }
    }
}
