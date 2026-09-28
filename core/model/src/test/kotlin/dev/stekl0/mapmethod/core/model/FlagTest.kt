package dev.stekl0.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlagTest {
    private val west = 0xFF000001.toInt()
    private val middle = 0xFF000002.toInt()
    private val east = 0xFF000003.toInt()
    private val tricolour = Flag(bands = listOf(west, middle, east))

    @Test
    fun `western third columns take the western band`() {
        assertEquals(west, tricolour.colorAt(col = 0, cols = 30))
        assertEquals(west, tricolour.colorAt(col = 9, cols = 30))
    }

    @Test
    fun `middle third columns take the middle band`() {
        assertEquals(middle, tricolour.colorAt(col = 10, cols = 30))
        assertEquals(middle, tricolour.colorAt(col = 19, cols = 30))
    }

    @Test
    fun `eastern third columns take the eastern band`() {
        assertEquals(east, tricolour.colorAt(col = 20, cols = 30))
        assertEquals(east, tricolour.colorAt(col = 29, cols = 30))
    }

    @Test
    fun `fourteen columns split five, five and four`() {
        val colors = (0 until 14).map { tricolour.colorAt(col = it, cols = 14) }

        assertEquals(List(5) { west } + List(5) { middle } + List(4) { east }, colors)
    }

    @Test
    fun `a single band colours every column`() {
        val plain = Flag(bands = listOf(middle))

        assertEquals(List(14) { middle }, (0 until 14).map { plain.colorAt(col = it, cols = 14) })
    }

    @Test
    fun `a flag has at least one band`() {
        assertFailsWith<IllegalArgumentException> { Flag(bands = emptyList()) }
    }
}
