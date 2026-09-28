package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.maxdubmors.mapmethod.core.model.Flag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

private val Screen = Size(1080f, 2200f)
private val Colors = listOf(Color.White, Color.Red, Color.DarkGray)

private fun burst() = confettiBurst(bounds = Screen, colors = Colors, random = Random(seed = 7))

private fun Confetti.positions() = pieces.map { Offset(it.x, it.y) }

class ConfettiTest {
    @Test
    fun `confetti comes in the Map's flag colours only`() {
        val flag = Flag(bands = Colors.map { it.toArgb() })

        assertEquals(Colors, confettiColors(flag))
    }

    @Test
    fun `a burst fires pieces in the given colours from inside the screen`() {
        val confetti = burst()

        assertTrue(confetti.pieces.isNotEmpty())
        assertFalse(confetti.isOver)
        for (piece in confetti.pieces) {
            assertTrue(piece.color in Colors)
            assertTrue(piece.x in 0f..Screen.width && piece.y in 0f..Screen.height)
        }
    }

    @Test
    fun `a burst on a screen not measured yet fires nothing`() {
        assertTrue(confettiBurst(bounds = Size.Zero, colors = Colors, random = Random(seed = 7)).isOver)
    }

    @Test
    fun `a long pause neither spawns pieces nor carries them further than a short one`() {
        val confetti = burst()

        val afterPause = confetti.step(frameMillis = 60_000L)
        val afterFrames = confetti.step(frameMillis = 100L)

        assertEquals(confetti.pieces.size, afterPause.pieces.size)
        assertEquals(afterFrames.positions(), afterPause.positions())
        assertFalse(afterPause.isOver)
    }

    @Test
    fun `a clock going backwards does not move pieces`() {
        val confetti = burst()

        assertEquals(confetti.positions(), confetti.step(frameMillis = -500L).positions())
    }

    @Test
    fun `pieces never multiply`() {
        var confetti = burst()
        val fired = confetti.pieces.size
        repeat(200) {
            confetti = confetti.step(frameMillis = 16L)
            assertTrue(confetti.pieces.size <= fired)
        }
    }

    @Test
    fun `burst pieces first fly up, then fall under gravity`() {
        val confetti = burst()
        val start = confetti.pieces.first().y

        var risen = confetti
        repeat(10) { risen = risen.step(frameMillis = 16L) }
        assertTrue(risen.pieces.first().y < start)
        var fallen = risen
        repeat(60) { fallen = fallen.step(frameMillis = 16L) }
        assertTrue(fallen.pieces.first().y > risen.pieces.first().y)
    }

    @Test
    fun `all pieces leave the screen and the effect ends within a few seconds`() {
        var confetti = burst()
        var elapsed = 0L
        while (!confetti.isOver && elapsed < 10_000L) {
            confetti = confetti.step(frameMillis = 16L)
            elapsed += 16L
        }

        assertTrue(confetti.isOver)
        assertTrue(confetti.pieces.isEmpty())
        assertTrue("took $elapsed ms", elapsed <= 6_000L)
    }
}
