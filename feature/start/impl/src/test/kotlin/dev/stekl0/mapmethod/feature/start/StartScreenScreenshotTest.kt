package dev.stekl0.mapmethod.feature.start

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.stekl0.mapmethod.core.screenshottesting.captureMultiTheme
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
    val composeRule = createComposeRule()

    @Test
    fun `Start with the motif assembled`() {
        composeRule.captureMultiTheme(name = "start", settleMillis = SettledMillis) {
            StartScreen(title = "MapMethod", onShowMap = {})
        }
    }
}
