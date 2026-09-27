package dev.stekl0.mapmethod.feature.start

import dev.stekl0.mapmethod.core.ui.CellStampMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotifAssemblyTest {
    @Test
    fun `first Cell starts stamping at once`() {
        assertEquals(0L, assemblySchedule(ItalyMotif.FILLED_COUNT).first())
    }

    @Test
    fun `Cells start one after another in fill order`() {
        val delays = assemblySchedule(ItalyMotif.FILLED_COUNT)
        assertEquals(ItalyMotif.FILLED_COUNT, delays.size)
        assertTrue(delays.zipWithNext().all { (earlier, later) -> later > earlier })
    }

    @Test
    fun `assembly ends with the last stamp in about 1_2 s`() {
        val delays = assemblySchedule(ItalyMotif.FILLED_COUNT)
        assertEquals(1_200L, delays.last() + CellStampMillis)
        assertEquals(1_200L, delays.assemblyDurationMillis())
    }

    @Test
    fun `a single Cell stamps at once`() {
        assertEquals(listOf(0L), assemblySchedule(1))
        assertEquals(CellStampMillis, assemblySchedule(1).assemblyDurationMillis())
    }

    @Test
    fun `no Cells means no assembly`() {
        assertEquals(emptyList<Long>(), assemblySchedule(0))
        assertEquals(0L, assemblySchedule(0).assemblyDurationMillis())
    }

    @Test
    fun `breath starts faint, peaks halfway and repeats`() {
        assertEquals(0f, breathAlpha(0L), 1e-6f)
        assertEquals(MaxBreathAlpha, breathAlpha(BreathPeriodMillis / 2), 1e-6f)
        assertEquals(breathAlpha(300L), breathAlpha(300L + BreathPeriodMillis), 1e-6f)
    }

    @Test
    fun `breath stays gentle`() {
        val alphas = (0L..BreathPeriodMillis step 50L).map(::breathAlpha)
        assertTrue(alphas.all { it in 0f..MaxBreathAlpha })
        assertTrue(MaxBreathAlpha < 0.5f)
    }
}
