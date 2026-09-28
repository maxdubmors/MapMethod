package dev.stekl0.mapmethod.core.screenshottesting

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.stekl0.mapmethod.core.designsystem.theme.MapMethodTheme
import org.robolectric.RuntimeEnvironment

/** The rule a screenshot test captures with: `createAndroidComposeRule<ComponentActivity>()`. */
public typealias ScreenshotTestRule =
    AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>

/**
 * A theme a screen is captured in. Without dynamic colour the capture pins the theme's own fallback
 * scheme; with it, the scheme drawn from the wallpaper colours Robolectric reports. The notebook
 * palette is the same either way.
 */
private enum class ScreenshotTheme(
    val darkTheme: Boolean,
    val dynamicColor: Boolean,
) {
    LIGHT(darkTheme = false, dynamicColor = false),
    DARK(darkTheme = true, dynamicColor = false),
    LIGHT_DYNAMIC(darkTheme = false, dynamicColor = true),
    DARK_DYNAMIC(darkTheme = true, dynamicColor = true),
}

/** A window size a screen is captured at, as Robolectric qualifiers. */
private enum class ScreenshotDevice(
    val qualifiers: String,
) {
    PHONE(RobolectricDeviceQualifiers.MediumPhone),
    FOLDABLE(RobolectricDeviceQualifiers.PixelFold),
    TABLET(RobolectricDeviceQualifiers.MediumTablet),
}

/** One capture: the baseline's [fileName], its [theme] and, unless the test's own, its [device]. */
private data class Shot(
    val fileName: String,
    val theme: ScreenshotTheme,
    val device: ScreenshotDevice? = null,
)

/**
 * Captures [content] inside the app theme, light and dark, each without and with dynamic colour, as
 * `<name>_light.png`, `<name>_dark.png`, `<name>_light_dynamic.png` and `<name>_dark_dynamic.png`,
 * on the device the test is configured for. The test clock moves only when told,
 * so nothing moves on its own: each capture composes [content] afresh, and the clock then runs
 * [settleMillis] so an animation reaches the frame the baseline pins.
 */
public fun ScreenshotTestRule.captureMultiTheme(
    name: String,
    settleMillis: Long = 0L,
    content: @Composable () -> Unit,
) {
    val shots = ScreenshotTheme.entries.map { Shot(fileName = "${name}_${it.name.lowercase()}.png", theme = it) }
    captureShots(shots, settleMillis, content)
}

/**
 * Captures [content] in the light app theme without dynamic colour on a phone, an unfolded foldable and a tablet, as
 * `<name>_phone.png`, `<name>_foldable.png` and `<name>_tablet.png`, so a layout is pinned at every
 * window size. The clock runs as in [captureMultiTheme].
 */
public fun ScreenshotTestRule.captureMultiDevice(
    name: String,
    settleMillis: Long = 0L,
    content: @Composable () -> Unit,
) {
    val shots =
        ScreenshotDevice.entries.map {
            Shot(fileName = "${name}_${it.name.lowercase()}.png", theme = ScreenshotTheme.LIGHT, device = it)
        }
    captureShots(shots, settleMillis, content)
}

private fun ScreenshotTestRule.captureShots(
    shots: List<Shot>,
    settleMillis: Long,
    content: @Composable () -> Unit,
) {
    mainClock.autoAdvance = false
    // Every shot is captured even when one differs from its baseline, so each leaves its own diff.
    val failures =
        shots.mapNotNull { shot ->
            // New qualifiers recreate the activity. A fresh view composes at once with fresh
            // effects, so every shot's animations start alike instead of running on from the last.
            shot.device?.let { RuntimeEnvironment.setQualifiers(it.qualifiers) }
            val view =
                ComposeView(activity).apply {
                    setContent {
                        MapMethodTheme(darkTheme = shot.theme.darkTheme, dynamicColor = shot.theme.dynamicColor) {
                            Surface { content() }
                        }
                    }
                }
            activity.setContentView(view)
            mainClock.advanceTimeBy(settleMillis)
            runCatching { onRoot().captureRoboImage(shot.fileName) }.exceptionOrNull()
        }
    failures.firstOrNull()?.let { first ->
        failures.drop(1).forEach(first::addSuppressed)
        throw first
    }
}
