package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Test

public class FlagBandTest {
    @Test
    public fun `northern half rows take the white band`() {
        assertEquals(FlagBand.WHITE, bandForRow(row = 0, rows = 30))
        assertEquals(FlagBand.WHITE, bandForRow(row = 14, rows = 30))
    }

    @Test
    public fun `southern half rows take the red band`() {
        assertEquals(FlagBand.RED, bandForRow(row = 15, rows = 30))
        assertEquals(FlagBand.RED, bandForRow(row = 29, rows = 30))
    }

    @Test
    public fun `odd row counts split toward red`() {
        assertEquals(FlagBand.WHITE, bandForRow(row = 0, rows = 3))
        assertEquals(FlagBand.RED, bandForRow(row = 1, rows = 3))
        assertEquals(FlagBand.RED, bandForRow(row = 2, rows = 3))
    }
}
