package dev.stekl0.mapmethod

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.stekl0.mapmethod.ui.theme.MapMethodTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppThemeInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appBackgroundUsesDarkThemeSurface() {
        var expectedBackground = Color.Unspecified
        composeRule.setContent {
            MapMethodTheme(darkTheme = true, dynamicColor = false) {
                expectedBackground = MaterialTheme.colorScheme.surface
                Surface(modifier = Modifier.fillMaxSize()) {
                }
            }
        }

        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        assertEquals(expectedBackground.toArgb(), pixels[pixels.width - 1, pixels.height / 2].toArgb())
    }
}
