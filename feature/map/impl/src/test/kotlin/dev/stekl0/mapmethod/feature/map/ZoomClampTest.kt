package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

public class ZoomClampTest {
    private val viewport = Size(1000f, 2000f)
    private val content = Size(1000f, 1000f)

    @Test
    public fun `scale one keeps zero offset`() {
        assertEquals(
            Offset.Zero,
            clampZoomOffset(Offset(300f, -500f), 1f, viewport, content),
        )
    }

    @Test
    public fun `pan clamps symmetrically to scaled overflow`() {
        // Scale 2: content 2000x2000 against 1000x2000 viewport: x range +-500, y range 0.
        assertEquals(
            Offset(500f, 0f),
            clampZoomOffset(Offset(2000f, 700f), 2f, viewport, content),
        )
        assertEquals(
            Offset(-500f, 0f),
            clampZoomOffset(Offset(-2000f, -700f), 2f, viewport, content),
        )
    }

    @Test
    public fun `in-bounds pan passes through`() {
        assertEquals(
            Offset(200f, 0f),
            clampZoomOffset(Offset(200f, 100f), 2f, viewport, content),
        )
    }

    @Test
    public fun `smaller scaled axis stays centered`() {
        // Scale 4 on a square canvas region smaller than the viewport height.
        val wide = Size(1000f, 400f)
        assertEquals(
            Offset(0f, 0f),
            clampZoomOffset(Offset(999f, 999f), 1f, viewport, wide),
        )
    }
}
