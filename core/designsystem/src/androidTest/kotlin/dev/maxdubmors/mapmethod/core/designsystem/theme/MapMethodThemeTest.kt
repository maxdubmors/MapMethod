package dev.maxdubmors.mapmethod.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapMethodThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightFallbackDrawsTheAppBackgroundOnPaper() {
        composeRule.setContent {
            MapMethodTheme(darkTheme = false, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                }
            }
        }

        assertEquals(PAPER.toArgb(), backgroundPixel())
    }

    @Test
    fun darkFallbackDrawsTheAppBackgroundOnGraphite() {
        composeRule.setContent {
            MapMethodTheme(darkTheme = true, dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                }
            }
        }

        assertEquals(DARK_GRAPHITE.toArgb(), backgroundPixel())
    }

    @Test
    fun fallbackUsesGraphitePrimaryInBothThemes() {
        var lightPrimary = Color.Unspecified
        var darkPrimary = Color.Unspecified
        composeRule.setContent {
            MapMethodTheme(darkTheme = false, dynamicColor = false) {
                lightPrimary = MaterialTheme.colorScheme.primary
            }
            MapMethodTheme(darkTheme = true, dynamicColor = false) {
                darkPrimary = MaterialTheme.colorScheme.primary
            }
        }

        composeRule.runOnIdle {
            assertEquals(GRAPHITE_PRIMARY_LIGHT, lightPrimary)
            assertEquals(GRAPHITE_PRIMARY_DARK, darkPrimary)
        }
    }

    @Test
    fun dynamicColorStillFollowsTheWallpaperOnAndroid12AndLater() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        var light: ColorScheme? = null
        var expectedLight: ColorScheme? = null
        var dark: ColorScheme? = null
        var expectedDark: ColorScheme? = null
        composeRule.setContent {
            val context = LocalContext.current
            expectedLight = dynamicLightColorScheme(context)
            expectedDark = dynamicDarkColorScheme(context)
            MapMethodTheme(darkTheme = false, dynamicColor = true) {
                light = MaterialTheme.colorScheme
            }
            MapMethodTheme(darkTheme = true, dynamicColor = true) {
                dark = MaterialTheme.colorScheme
            }
        }

        // ColorScheme has no equals(); its toString() lists every role, so it compares whole schemes.
        composeRule.runOnIdle {
            assertEquals(expectedLight.toString(), light.toString())
            assertEquals(expectedDark.toString(), dark.toString())
        }
    }

    @Test
    fun notebookPaletteFollowsDarkThemeButNeverTheWallpaper() {
        val palettes = mutableMapOf<Pair<Boolean, Boolean>, NotebookPalette>()
        composeRule.setContent {
            for (darkTheme in listOf(false, true)) {
                for (dynamicColor in listOf(false, true)) {
                    MapMethodTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                        palettes[darkTheme to dynamicColor] = LocalNotebookPalette.current
                    }
                }
            }
        }

        composeRule.runOnIdle {
            assertEquals(NotebookPalette.Light, palettes[false to false])
            assertEquals(NotebookPalette.Light, palettes[false to true])
            assertEquals(NotebookPalette.Dark, palettes[true to false])
            assertEquals(NotebookPalette.Dark, palettes[true to true])
        }
    }

    private fun backgroundPixel(): Int {
        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        return pixels[pixels.width - 1, pixels.height / 2].toArgb()
    }

    private companion object {
        // Known-good values of the generated paper-and-graphite scheme (seed #3C4043).
        val PAPER = Color(0xFFFBF9F9)
        val DARK_GRAPHITE = Color(0xFF131314)
        val GRAPHITE_PRIMARY_LIGHT = Color(0xFF536069)
        val GRAPHITE_PRIMARY_DARK = Color(0xFFBBC8D2)
    }
}
