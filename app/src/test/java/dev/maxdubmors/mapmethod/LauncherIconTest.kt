package dev.maxdubmors.mapmethod

import dev.maxdubmors.mapmethod.feature.start.ItalyMotif
import dev.maxdubmors.mapmethod.feature.start.MotifCell
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
    fun `foreground draws filled and empty motif Cells`() {
        assertMotifPaths("ic_launcher_foreground")
    }

    @Test
    fun `monochrome draws filled motif Cells solid and empty ones faint`() {
        assertMotifPaths("ic_launcher_monochrome")
        val emptyAlpha = paths("ic_launcher_monochrome").getValue("empty").getValue("android:fillAlpha").toFloat()
        assertTrue("empty Cells alpha $emptyAlpha", emptyAlpha > 0f && emptyAlpha < 1f)
    }

    @Test
    fun `every Cell lies inside the 66 dp safe zone`() {
        val farthest =
            cells.maxOf { cell ->
                listOf(0, 1).maxOf { dx ->
                    listOf(0, 1).maxOf { dy ->
                        val x = xTenths(cell, dx * CELL_TENTHS) / 10.0
                        val y = yTenths(cell, dy * CELL_TENTHS) / 10.0
                        hypot(x - CENTER, y - CENTER)
                    }
                }
            }
        assertTrue("farthest Cell corner at $farthest dp", farthest <= SAFE_RADIUS)
    }

    private fun assertMotifPaths(drawable: String) {
        val paths = paths(drawable)
        assertEquals(cellsPath(cells.filter { it.filled }), paths.getValue("filled").getValue("android:pathData"))
        assertEquals(cellsPath(cells.filterNot { it.filled }), paths.getValue("empty").getValue("android:pathData"))
    }

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
            val byName =
                (0 until attributes.length).associate { attributes.item(it).nodeName to attributes.item(it).nodeValue }
            byName.getValue("android:name") to byName
        }
    }

    // Coordinates in tenths of a dp keep the pathData free of floating point noise.
    private fun cellsPath(cells: List<MotifCell>): String =
        cells.joinToString("") { cell ->
            val size = pathNumber(CELL_TENTHS - 2 * INSET_TENTHS)
            val x = pathNumber(xTenths(cell, INSET_TENTHS))
            val y = pathNumber(yTenths(cell, INSET_TENTHS))
            "M$x,${y}h${size}v${size}h-${size}z"
        }

    private fun xTenths(cell: MotifCell, offset: Int): Int = (ORIGIN_COL + cell.col) * CELL_TENTHS + offset

    private fun yTenths(cell: MotifCell, offset: Int): Int = (ORIGIN_ROW + cell.row) * CELL_TENTHS + offset

    private fun pathNumber(tenths: Int): String =
        if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"

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
