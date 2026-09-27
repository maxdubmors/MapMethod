package dev.stekl0.mapmethod

import dev.stekl0.mapmethod.feature.start.ItalyMotif
import dev.stekl0.mapmethod.feature.start.MotifCell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.hypot

/**
 * Keeps the launcher icon layers in sync with [ItalyMotif]. On a mismatch the failure
 * message carries the expected pathData to paste into the drawable.
 */
class LauncherIconTest {
    private val cells = ItalyMotif.cells()

    @Test
    fun `foreground draws shaded and empty motif Cells`() {
        val paths = pathData("ic_launcher_foreground")
        assertEquals(cellsPath(cells.filter { it.shaded }), paths["shaded"])
        assertEquals(cellsPath(cells.filterNot { it.shaded }), paths["empty"])
    }

    @Test
    fun `monochrome draws shaded motif Cells solid and empty ones faint`() {
        val paths = pathData("ic_launcher_monochrome")
        assertEquals(cellsPath(cells.filter { it.shaded }), paths["shaded"])
        assertEquals(cellsPath(cells.filterNot { it.shaded }), paths["empty"])
        val emptyAlpha = attribute("ic_launcher_monochrome", "empty", "android:fillAlpha").toFloat()
        assertTrue("empty Cells alpha $emptyAlpha", emptyAlpha > 0f && emptyAlpha < 1f)
    }

    @Test
    fun `every Cell lies inside the 66 dp safe zone`() {
        val farthest =
            cells.maxOf { cell ->
                listOf(0, 1).maxOf { dx ->
                    listOf(0, 1).maxOf { dy ->
                        hypot(x(cell, dx * CELL_TENTHS) / 10.0 - CENTER, y(cell, dy * CELL_TENTHS) / 10.0 - CENTER)
                    }
                }
            }
        assertTrue("farthest Cell corner at $farthest dp", farthest <= SAFE_RADIUS)
    }

    private fun pathData(drawable: String): Map<String, String> =
        paths(drawable).mapValues { (_, attributes) -> attributes.getValue("android:pathData") }

    private fun attribute(drawable: String, path: String, name: String): String =
        paths(drawable).getValue(path).getValue(name)

    /** Attributes of every `<path>` in the drawable, keyed by `android:name`. */
    private fun paths(drawable: String): Map<String, Map<String, String>> {
        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(File("src/main/res/drawable/$drawable.xml"))
        val nodes = document.getElementsByTagName("path")
        return (0 until nodes.length).associate { index ->
            val attributes = nodes.item(index).attributes
            val byName = (0 until attributes.length).associate { attributes.item(it).nodeName to attributes.item(it).nodeValue }
            byName.getValue("android:name") to byName
        }
    }

    // Coordinates in tenths of a dp keep the pathData free of floating point noise.
    private fun cellsPath(cells: List<MotifCell>): String =
        cells.joinToString("") { cell ->
            val size = dp(CELL_TENTHS - 2 * INSET_TENTHS)
            "M${dp(x(cell, INSET_TENTHS))},${dp(y(cell, INSET_TENTHS))}h${size}v${size}h-${size}z"
        }

    private fun x(cell: MotifCell, offset: Int): Int = (ORIGIN_COL + cell.col) * CELL_TENTHS + offset

    private fun y(cell: MotifCell, offset: Int): Int = (ORIGIN_ROW + cell.row) * CELL_TENTHS + offset

    private fun dp(tenths: Int): String = if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"

    private companion object {
        /** Motif placement on the 108 dp icon grid of 3 dp Cells. */
        const val ORIGIN_COL = 10
        const val ORIGIN_ROW = 9
        const val CELL_TENTHS = 30
        const val INSET_TENTHS = 3
        const val CENTER = 54.0
        const val SAFE_RADIUS = 33.0
    }
}
