package dev.stekl0.mapmethod.feature.map

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.stekl0.mapmethod.core.screenshottesting.ScreenshotFrance
import dev.stekl0.mapmethod.core.screenshottesting.captureMultiDevice
import dev.stekl0.mapmethod.core.screenshottesting.captureMultiTheme
import dev.stekl0.mapmethod.core.screenshottesting.screenshotFrance
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** France with its first [filledCount] Cells in fill order filled. */
private fun franceMap(filledCount: Int): MapUiState = MapUiState(map = screenshotFrance(filledCount), isLoaded = true)

@Composable
private fun MapScreenOf(state: MapUiState) {
    MapScreen(
        state = state,
        cascade = rememberLogCascadeState(state),
        completion = rememberCompletionState(state),
        onLogCount = {},
        onBack = {},
    )
}

/**
 * The Map as its state alone draws it: no Log cascade or Completion plays, so every Cell is
 * settled and the capture needs no clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.MediumPhone)
class MapScreenScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `empty Map with the next Cell previewed`() {
        captureMap(name = "map_empty", state = franceMap(filledCount = 0))
    }

    @Test
    fun `partly filled Map in pencil`() {
        captureMap(name = "map_partly_filled", state = franceMap(filledCount = 40))
    }

    @Test
    fun `complete Map in the flag colours`() {
        captureMap(name = "map_complete", state = franceMap(filledCount = ScreenshotFrance.cells.size))
    }

    @Test
    fun `partly filled Map on every window size`() {
        val state = franceMap(filledCount = 40)
        composeRule.captureMultiDevice(name = "map_partly_filled") { MapScreenOf(state) }
    }

    private fun captureMap(name: String, state: MapUiState) {
        composeRule.captureMultiTheme(name = name) { MapScreenOf(state) }
    }
}
