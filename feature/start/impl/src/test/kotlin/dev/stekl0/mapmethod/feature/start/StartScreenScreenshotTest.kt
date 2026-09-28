package dev.stekl0.mapmethod.feature.start

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.stekl0.mapmethod.core.designsystem.theme.MapMethodTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Past the last stamp, so the motif is assembled; the preview Cell's breath is still at its faintest.
private val SettledMillis = assemblySchedule(ItalyMotif.FILLED_COUNT).assemblyDurationMillis() + 100L

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class StartScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Start with the motif assembled, light`() {
        captureStart(darkTheme = false, name = "start_light")
    }

    @Test
    fun `Start with the motif assembled, dark`() {
        captureStart(darkTheme = true, name = "start_dark")
    }

    private fun captureStart(darkTheme: Boolean, name: String) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            // The notebook palette of the theme itself, not the colours of the device's wallpaper.
            MapMethodTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface {
                    StartScreen(title = "MapMethod", onShowMap = {})
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(SettledMillis)
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
