package dev.stekl0.mapmethod.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Fixed colours of the notebook sheet the Map is drawn on. Unlike [androidx.compose.material3.ColorScheme]
 * they never follow the wallpaper, so pencil always looks like pencil.
 *
 * @property paper the sheet itself.
 * @property gridLine the faint grid printed across the whole sheet.
 * @property countryTint a country's Cell that is not filled yet.
 * @property graphite a filled Cell while its Map is in progress.
 * @property previewOutline the dashed outline of the next Cells a Log would fill.
 * @property flagWhite the white band of the Polish flag, the Map's colour at Completion.
 * @property flagRed the red band of the Polish flag, the Map's colour at Completion.
 */
@Immutable
public data class NotebookPalette(
    val paper: Color,
    val gridLine: Color,
    val countryTint: Color,
    val graphite: Color,
    val previewOutline: Color,
    val flagWhite: Color,
    val flagRed: Color,
) {
    public companion object {
        // Paper, grid line and graphite match the launcher icon.
        public val Light: NotebookPalette =
            NotebookPalette(
                paper = Color(0xFFFBF8EF),
                gridLine = Color(0xFFE2DBC8),
                countryTint = Color(0xFFEFE9D9),
                graphite = Color(0xFF45474A),
                previewOutline = Color(0xFF45474A),
                flagWhite = Color(0xFFFFFFFF),
                flagRed = Color(0xFFDC143C),
            )

        // Dark paper with light, chalk-like graphite, so the Map never glares at night.
        public val Dark: NotebookPalette =
            NotebookPalette(
                paper = Color(0xFF2A2A28),
                gridLine = Color(0xFF3D3D39),
                countryTint = Color(0xFF33332F),
                graphite = Color(0xFFC9C9C4),
                previewOutline = Color(0xFFC9C9C4),
                flagWhite = Color(0xFFEAE8E2),
                flagRed = Color(0xFFDC143C),
            )
    }
}

/** The [NotebookPalette] of the current theme, provided by [MapMethodTheme]. */
public val LocalNotebookPalette: ProvidableCompositionLocal<NotebookPalette> =
    staticCompositionLocalOf { NotebookPalette.Light }
