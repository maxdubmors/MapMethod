package dev.maxdubmors.mapmethod.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class NotebookPaletteContrastTest {
    @Test
    fun `graphite on paper reads like text in the light theme`() {
        assertContrast(NotebookPalette.Light.graphite, NotebookPalette.Light.paper, MIN_CONTRAST)
    }

    @Test
    fun `graphite on paper reads like text in the dark theme`() {
        assertContrast(NotebookPalette.Dark.graphite, NotebookPalette.Dark.paper, MIN_CONTRAST)
    }

    @Test
    fun `filled Cells stand out from empty Cells in the light theme`() {
        assertContrast(NotebookPalette.Light.graphite, NotebookPalette.Light.countryTint, MIN_CONTRAST)
    }

    @Test
    fun `filled Cells stand out from empty Cells in the dark theme`() {
        assertContrast(NotebookPalette.Dark.graphite, NotebookPalette.Dark.countryTint, MIN_CONTRAST)
    }

    @Test
    fun `the preview outline stands out on empty Cells in the light theme`() {
        assertContrast(NotebookPalette.Light.previewOutline, NotebookPalette.Light.countryTint, MIN_GRAPHIC_CONTRAST)
    }

    @Test
    fun `the preview outline stands out on empty Cells in the dark theme`() {
        assertContrast(NotebookPalette.Dark.previewOutline, NotebookPalette.Dark.countryTint, MIN_GRAPHIC_CONTRAST)
    }

    @Test
    fun `dark paper carries chalk-light graphite while light paper carries dark graphite`() {
        assertTrue(NotebookPalette.Dark.paper.luminance() < NotebookPalette.Dark.graphite.luminance())
        assertTrue(NotebookPalette.Light.paper.luminance() > NotebookPalette.Light.graphite.luminance())
    }

    private fun assertContrast(foreground: Color, background: Color, minimum: Double) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("Contrast %.2f is below %.1f:1".format(ratio, minimum), ratio >= minimum)
    }

    // WCAG 2.x contrast ratio, the measure behind Material's contrast minimums.
    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05) / (darker + 0.05)
    }

    private companion object {
        // Material's text minimum: stricter than the 3:1 for graphics, so filled Cells read at a glance.
        const val MIN_CONTRAST = 4.5

        // Material's minimum for graphics, enough for a dashed outline.
        const val MIN_GRAPHIC_CONTRAST = 3.0
    }
}
