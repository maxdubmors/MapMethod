package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Test

class FlagBandTest {
    @Test
    fun `western third columns take the blue band`() {
        assertEquals(FlagBand.BLUE, bandForCol(col = 0, cols = 30))
        assertEquals(FlagBand.BLUE, bandForCol(col = 9, cols = 30))
    }

    @Test
    fun `middle third columns take the white band`() {
        assertEquals(FlagBand.WHITE, bandForCol(col = 10, cols = 30))
        assertEquals(FlagBand.WHITE, bandForCol(col = 19, cols = 30))
    }

    @Test
    fun `eastern third columns take the red band`() {
        assertEquals(FlagBand.RED, bandForCol(col = 20, cols = 30))
        assertEquals(FlagBand.RED, bandForCol(col = 29, cols = 30))
    }

    @Test
    fun `fourteen columns split five, five and four`() {
        val bands = (0 until 14).map { bandForCol(col = it, cols = 14) }

        assertEquals(
            List(5) { FlagBand.BLUE } + List(5) { FlagBand.WHITE } + List(4) { FlagBand.RED },
            bands,
        )
    }
}
