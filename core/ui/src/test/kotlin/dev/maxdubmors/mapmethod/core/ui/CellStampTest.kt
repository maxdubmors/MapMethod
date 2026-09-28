package dev.maxdubmors.mapmethod.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CellStampTest {
    @Test
    fun `a Cell that has not started stamping is invisible`() {
        assertEquals(0f, stampAlpha(-1L))
        assertEquals(0f, stampAlpha(0L))
    }

    @Test
    fun `a stamped Cell is full size and opaque`() {
        assertEquals(1f, stampScale(CellStampMillis))
        assertEquals(1f, stampAlpha(CellStampMillis))
        assertEquals(1f, stampScale(Long.MAX_VALUE))
    }

    @Test
    fun `the stamp overshoots slightly`() {
        val peak = (0L..CellStampMillis).maxOf { stampScale(it) }

        assertTrue("peak $peak", peak > 1f)
        assertTrue("peak $peak", peak < 1.1f)
    }

    @Test
    fun `the stamp has settled before it snaps to full size`() {
        val almost = stampScale(CellStampMillis - 1)

        assertEquals(1f, almost, 0.01f)
    }

    @Test
    fun `the fade only brightens`() {
        val alphas = (0L..CellStampMillis).map { stampAlpha(it) }

        assertEquals(alphas.sorted(), alphas)
    }
}
