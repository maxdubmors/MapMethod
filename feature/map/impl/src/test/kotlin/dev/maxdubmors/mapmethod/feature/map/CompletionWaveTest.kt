package dev.maxdubmors.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionWaveTest {
    @Test
    fun `an empty Map has no wave`() {
        assertEquals(emptyList<WaveRecolour>(), completionWave(emptyList()))
        assertEquals(0L, completionWave(emptyList()).waveDurationMillis())
    }

    @Test
    fun `Cells recolour in fill order`() {
        val wave = completionWave(listOf(2, 0, 3, 1))

        assertEquals(listOf(0, 1, 2, 3), wave.map { it.position })
        val delays = wave.map { it.delayMillis }
        assertEquals(delays.sorted(), delays)
        assertTrue("delays $delays", delays.first() < delays.last())
    }

    @Test
    fun `the wave starts at once`() {
        assertEquals(0L, completionWave(List(50) { it }).first().delayMillis)
    }

    @Test
    fun `the whole wave takes about one second for small and large Maps`() {
        for (count in listOf(2, 50, 2_500)) {
            val duration = completionWave(List(count) { it }).waveDurationMillis()
            assertTrue("count $count takes $duration ms", duration in 900L..1_100L)
        }
    }

    @Test
    fun `the wave ends when its last Cell has recoloured`() {
        val wave = completionWave(List(50) { it })

        assertEquals(wave.last().delayMillis + CellRecolourMillis, wave.waveDurationMillis())
    }
}
