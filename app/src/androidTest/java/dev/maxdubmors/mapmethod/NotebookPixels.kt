package dev.maxdubmors.mapmethod

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.test.platform.app.InstrumentationRegistry
import dev.maxdubmors.mapmethod.core.data.catalogue.France
import dev.maxdubmors.mapmethod.core.designsystem.theme.NotebookPalette
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/** The notebook palette of the system theme the app follows, which Start, the Atlas and the Map draw in. */
internal fun systemNotebookPalette(): NotebookPalette {
    val configuration = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration
    val nightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return if (nightMode == Configuration.UI_MODE_NIGHT_YES) NotebookPalette.Dark else NotebookPalette.Light
}

/** Every pixel of the bitmap as an ARGB colour. */
internal fun Bitmap.argbPixels(): IntArray {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return pixels
}

/** Every pixel of the node as it shows now, as ARGB colours. */
internal fun SemanticsNodeInteraction.capturePixels(): IntArray = captureToImage().asAndroidBitmap().argbPixels()

/** France drawn at Completion: each of its own flag colours shows, whatever the theme, and no graphite. */
internal fun assertFranceInFlagColours(pixels: IntArray) {
    val shown = pixels.toSet()
    France.flag.bands.forEach { assertTrue(it in shown) }
    assertFalse(systemNotebookPalette().graphite.toArgb() in shown)
}

/** France drawn in pencil: its filled Cells are graphite, and neither its western nor its eastern band shows. */
internal fun assertFranceInPencil(pixels: IntArray) {
    val shown = pixels.toSet()
    assertTrue(systemNotebookPalette().graphite.toArgb() in shown)
    assertFalse(France.flag.bands.first() in shown)
    assertFalse(France.flag.bands.last() in shown)
}
