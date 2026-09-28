package dev.stekl0.mapmethod.core.screenshottesting

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import dev.stekl0.mapmethod.core.designsystem.theme.MapMethodTheme

private enum class ScreenshotTheme(
    val darkTheme: Boolean,
) {
    LIGHT(darkTheme = false),
    DARK(darkTheme = true),
}

/**
 * Captures [content] inside the app theme, light then dark, as `<name>_light.png` and
 * `<name>_dark.png`. The test clock moves only when told, so nothing moves on its own: each theme
 * composes [content] afresh, and the clock then runs [settleMillis] so an animation reaches the
 * frame the baseline pins. Call it once per test, before anything else sets the content.
 */
public fun ComposeContentTestRule.captureMultiTheme(
    name: String,
    settleMillis: Long = 0L,
    content: @Composable () -> Unit,
) {
    var theme by mutableStateOf(ScreenshotTheme.entries.first())
    mainClock.autoAdvance = false
    setContent {
        // Dynamic colour follows the device's wallpaper, so the capture pins the theme's own
        // fallback scheme instead; the notebook palette is the same either way.
        MapMethodTheme(darkTheme = theme.darkTheme, dynamicColor = false) {
            // Keyed so every theme starts its animations from the beginning.
            key(theme) {
                Surface { content() }
            }
        }
    }
    // Every theme is captured even when one differs from its baseline, so each leaves its own diff.
    val failures =
        ScreenshotTheme.entries.mapNotNull { next ->
            if (theme != next) {
                theme = next
                // The new theme composes on the next frame.
                mainClock.advanceTimeByFrame()
            }
            mainClock.advanceTimeBy(settleMillis)
            runCatching { onRoot().captureRoboImage("${name}_${next.name.lowercase()}.png") }.exceptionOrNull()
        }
    failures.firstOrNull()?.let { first ->
        failures.drop(1).forEach(first::addSuppressed)
        throw first
    }
}
