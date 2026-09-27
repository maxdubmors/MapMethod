package dev.stekl0.mapmethod.feature.map

import dev.stekl0.mapmethod.core.ui.CellStampMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CascadeScheduleTest {
    @Test
    fun `nothing filled schedules nothing`() {
        assertEquals(emptyList<CascadeStamp>(), cascadeSchedule(emptyList()))
    }

    @Test
    fun `a single Cell stamps at once`() {
        assertEquals(0L, cascadeSchedule(listOf(7)).single().delayMillis)
    }

    @Test
    fun `Cells stamp in fill order`() {
        assertEquals(listOf(3, 4, 5, 6), cascadeSchedule(listOf(5, 3, 6, 4)).map { it.orderIndex })
    }

    @Test
    fun `delays never go back in time`() {
        for (count in listOf(2, 10, 50, 2_500)) {
            val delays = cascadeSchedule(List(count) { it }).map { it.delayMillis }
            assertEquals("count $count", delays.sorted(), delays)
            assertEquals("count $count", 0L, delays.first())
        }
    }

    @Test
    fun `the whole cascade fits about 700 ms for small and large counts`() {
        for (count in listOf(1, 2, 5, 10, 50, 500, 2_500)) {
            val last = cascadeSchedule(List(count) { it }).last()
            assertTrue("count $count ends at ${last.delayMillis}", (last.delayMillis + CellStampMillis) <= 700L)
        }
    }

    @Test
    fun `the stagger shrinks as the count grows`() {
        fun stagger(count: Int) = cascadeSchedule(List(count) { it })[1].delayMillis

        assertTrue(stagger(50) < stagger(5))
        assertTrue(stagger(2_500) <= stagger(50))
    }

    @Test
    fun `a few Cells are spread out enough to read one by one`() {
        val delays = cascadeSchedule(List(3) { it }).map { it.delayMillis }

        assertTrue("delays $delays", delays[1] >= 40L)
    }

    @Test
    fun `only about the first ten Cells tick`() {
        val ticks = cascadeSchedule(List(50) { it }).map { it.hasTick }

        assertEquals(List(10) { true } + List(40) { false }, ticks)
    }

    @Test
    fun `the cascade ends when its last Cell has stamped`() {
        val schedule = cascadeSchedule(List(50) { it })

        assertEquals(schedule.last().delayMillis + CellStampMillis, schedule.durationMillis())
        assertEquals(0L, emptyList<CascadeStamp>().durationMillis())
    }
}
