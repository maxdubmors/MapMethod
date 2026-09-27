package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressFractionTest {
    @Test
    fun `fraction is the filled share of the Map`() {
        assertEquals(0f, progressFraction(filled = 0f, total = 40), 0f)
        assertEquals(0.25f, progressFraction(filled = 10f, total = 40), 0f)
        assertEquals(1f, progressFraction(filled = 40f, total = 40), 0f)
    }

    @Test
    fun `a Map that is not loaded yet shows no progress`() {
        assertEquals(0f, progressFraction(filled = 0f, total = 0), 0f)
    }

    @Test
    fun `an overshooting animation stays within the bar`() {
        assertEquals(1f, progressFraction(filled = 41f, total = 40), 0f)
        assertEquals(0f, progressFraction(filled = -1f, total = 40), 0f)
    }
}
