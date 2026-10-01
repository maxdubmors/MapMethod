package dev.maxdubmors.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogCountTest {
    @Test
    fun `valid entry parses as typed`() {
        assertEquals(3, parseLogCount(text = "3", emptyCount = 10))
    }

    @Test
    fun `entry beyond the empty Cells clamps while typing`() {
        assertEquals(3, parseLogCount(text = "999", emptyCount = 3))
    }

    @Test
    fun `empty zero and negative entries are invalid`() {
        assertNull(parseLogCount(text = "", emptyCount = 10))
        assertNull(parseLogCount(text = "0", emptyCount = 10))
        assertNull(parseLogCount(text = "-4", emptyCount = 10))
        assertNull(parseLogCount(text = "ten", emptyCount = 10))
    }

    @Test
    fun `steppers move from the current entry within bounds`() {
        assertEquals(6, stepLogCount(current = 1, step = 5, emptyCount = 10))
        assertEquals(1, stepLogCount(current = 2, step = -10, emptyCount = 10))
        assertEquals(3, stepLogCount(current = 1, step = 10, emptyCount = 3))
    }

    @Test
    fun `steppers from a cleared entry restart from one`() {
        assertEquals(1, stepLogCount(current = null, step = 5, emptyCount = 10))
        assertEquals(1, stepLogCount(current = null, step = -5, emptyCount = 10))
    }

    @Test
    fun `typed entry clamps live to the empty Cells`() {
        assertEquals("3", clampEntryText(typed = "3", current = "1", emptyCount = 10))
        assertEquals("3", clampEntryText(typed = "999", current = "1", emptyCount = 3))
    }

    @Test
    fun `empty typed entry clears`() {
        assertEquals("", clampEntryText(typed = "", current = "1", emptyCount = 10))
    }

    @Test
    fun `invalid typed entry keeps the current value`() {
        assertEquals("1", clampEntryText(typed = "0", current = "1", emptyCount = 10))
        assertEquals("1", clampEntryText(typed = "ten", current = "1", emptyCount = 10))
    }
}
