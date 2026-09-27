package dev.stekl0.mapmethod.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
@Suppress("ModifierRequired") // Theme wrappers emit no layout; cf. NiA NiaTheme with no modifier.
public fun MapMethodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            else -> {
                fallbackColorScheme(darkTheme)
            }
        }

    // Design tokens beyond MaterialTheme are provided here as static composition locals,
    // cf. NiA NiaTheme providing LocalBackgroundTheme and LocalTintTheme.
    val notebookPalette = if (darkTheme) NotebookPalette.Dark else NotebookPalette.Light
    CompositionLocalProvider(LocalNotebookPalette provides notebookPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
