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

// Just past the last stamp: the motif is assembled and the preview Cell's breath has barely begun.
private val SettledMillis = assemblySchedule(ItalyMotif.FILLED_COUNT).assemblyDurationMillis() + 100L

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class StartScreenScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `Start with the motif assembled, light`() {
        captureStart(darkTheme = false)
    }

    @Test
    fun `Start with the motif assembled, dark`() {
        captureStart(darkTheme = true)
    }

    private fun captureStart(darkTheme: Boolean) {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            // Dynamic colour follows the device's wallpaper, so the capture pins the theme's own
            // fallback scheme instead; the notebook palette is the same either way.
            MapMethodTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface {
                    StartScreen(title = "MapMethod", onShowMap = {})
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(SettledMillis)
        composeRule.onRoot().captureRoboImage(if (darkTheme) "start_dark.png" else "start_light.png")
    }
}
