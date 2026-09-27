package dev.stekl0.mapmethod.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackColorSchemeContrastTest {
    @Test
    fun `text on container roles meet the Material contrast minimum in the light theme`() {
        assertTextPairsMeetContrastMinimum(fallbackColorScheme(darkTheme = false))
    }

    @Test
    fun `text on container roles meet the Material contrast minimum in the dark theme`() {
        assertTextPairsMeetContrastMinimum(fallbackColorScheme(darkTheme = true))
    }

    private fun assertTextPairsMeetContrastMinimum(scheme: ColorScheme) {
        val failures =
            textPairs(scheme)
                .filter { pair -> pair.contrastRatio() < MIN_TEXT_CONTRAST }
        assertTrue(
            "Pairs below $MIN_TEXT_CONTRAST:1 — " +
                failures.joinToString { pair -> "${pair.name} %.2f".format(pair.contrastRatio()) },
            failures.isEmpty(),
        )
    }

    private fun textPairs(scheme: ColorScheme): List<TextPair> =
        with(scheme) {
            listOf(
                TextPair("onPrimary/primary", onPrimary, primary),
                TextPair("onPrimaryContainer/primaryContainer", onPrimaryContainer, primaryContainer),
                TextPair("onSecondary/secondary", onSecondary, secondary),
                TextPair("onSecondaryContainer/secondaryContainer", onSecondaryContainer, secondaryContainer),
                TextPair("onTertiary/tertiary", onTertiary, tertiary),
                TextPair("onTertiaryContainer/tertiaryContainer", onTertiaryContainer, tertiaryContainer),
                TextPair("onError/error", onError, error),
                TextPair("onErrorContainer/errorContainer", onErrorContainer, errorContainer),
                TextPair("onBackground/background", onBackground, background),
                TextPair("onSurface/surface", onSurface, surface),
                TextPair("onSurface/surfaceDim", onSurface, surfaceDim),
                TextPair("onSurface/surfaceBright", onSurface, surfaceBright),
                TextPair("onSurface/surfaceContainerLowest", onSurface, surfaceContainerLowest),
                TextPair("onSurface/surfaceContainerLow", onSurface, surfaceContainerLow),
                TextPair("onSurface/surfaceContainer", onSurface, surfaceContainer),
                TextPair("onSurface/surfaceContainerHigh", onSurface, surfaceContainerHigh),
                TextPair("onSurface/surfaceContainerHighest", onSurface, surfaceContainerHighest),
                TextPair("onSurfaceVariant/surfaceVariant", onSurfaceVariant, surfaceVariant),
                TextPair("onSurfaceVariant/surface", onSurfaceVariant, surface),
                TextPair("onSurfaceVariant/surfaceContainerHighest", onSurfaceVariant, surfaceContainerHighest),
                TextPair("inverseOnSurface/inverseSurface", inverseOnSurface, inverseSurface),
                TextPair("inversePrimary/inverseSurface", inversePrimary, inverseSurface),
                TextPair("primary/surface", primary, surface),
                TextPair("primary/surfaceContainerHighest", primary, surfaceContainerHighest),
                TextPair("error/surface", error, surface),
                TextPair("onPrimaryFixed/primaryFixed", onPrimaryFixed, primaryFixed),
                TextPair("onPrimaryFixedVariant/primaryFixed", onPrimaryFixedVariant, primaryFixed),
                TextPair("onSecondaryFixed/secondaryFixed", onSecondaryFixed, secondaryFixed),
                TextPair("onSecondaryFixedVariant/secondaryFixed", onSecondaryFixedVariant, secondaryFixed),
                TextPair("onTertiaryFixed/tertiaryFixed", onTertiaryFixed, tertiaryFixed),
                TextPair("onTertiaryFixedVariant/tertiaryFixed", onTertiaryFixedVariant, tertiaryFixed),
            )
        }

    private data class TextPair(
        val name: String,
        val text: Color,
        val container: Color,
    ) {
        // WCAG 2.x contrast ratio, the measure behind Material's 4.5:1 minimum for text.
        fun contrastRatio(): Double {
            val lighter = maxOf(text.luminance(), container.luminance())
            val darker = minOf(text.luminance(), container.luminance())
            return (lighter + 0.05) / (darker + 0.05)
        }
    }

    private companion object {
        const val MIN_TEXT_CONTRAST = 4.5
    }
}
