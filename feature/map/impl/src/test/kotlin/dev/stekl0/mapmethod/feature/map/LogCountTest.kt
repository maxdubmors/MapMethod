package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

public class LogCountTest {
    @Test
    public fun `valid entry parses as typed`() {
        assertEquals(3, parseLogCount(text = "3", remaining = 10))
    }

    @Test
    public fun `entry beyond remaining clamps while typing`() {
        assertEquals(3, parseLogCount(text = "999", remaining = 3))
    }

    @Test
    public fun `empty zero and negative entries are invalid`() {
        assertNull(parseLogCount(text = "", remaining = 10))
        assertNull(parseLogCount(text = "0", remaining = 10))
        assertNull(parseLogCount(text = "-4", remaining = 10))
        assertNull(parseLogCount(text = "ten", remaining = 10))
    }

    @Test
    public fun `steppers move from the current entry within bounds`() {
        assertEquals(6, stepLogCount(current = 1, step = 5, remaining = 10))
        assertEquals(1, stepLogCount(current = 2, step = -10, remaining = 10))
        assertEquals(3, stepLogCount(current = 1, step = 10, remaining = 3))
    }

    @Test
    public fun `steppers from a cleared entry restart from one`() {
        assertEquals(1, stepLogCount(current = null, step = 5, remaining = 10))
        assertEquals(1, stepLogCount(current = null, step = -5, remaining = 10))
    }

    @Test
    public fun `typed entry clamps live to remaining`() {
        assertEquals("3", clampEntryText(typed = "3", current = "1", remaining = 10))
        assertEquals("3", clampEntryText(typed = "999", current = "1", remaining = 3))
    }

    @Test
    public fun `empty typed entry clears`() {
        assertEquals("", clampEntryText(typed = "", current = "1", remaining = 10))
    }

    @Test
    public fun `invalid typed entry keeps the current value`() {
        assertEquals("1", clampEntryText(typed = "0", current = "1", remaining = 10))
        assertEquals("1", clampEntryText(typed = "ten", current = "1", remaining = 10))
    }
}
