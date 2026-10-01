package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import dev.maxdubmors.mapmethod.core.model.Cell
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.LogOutcome
import dev.maxdubmors.mapmethod.core.model.MapDefinition
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress
import dev.maxdubmors.mapmethod.core.ui.CellStampMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("MayBeConst")
private val FrameMillis = 16L

private val Graphite = Color(0xFF333333)

private val FlagRed = Color(0xFFFF0000)

private val FiveCells =
    MapDefinition(
        id = MapId("five"),
        cells = List(5) { Cell(orderIndex = it, row = 0, col = it) },
        flag = Flag(bands = listOf(0xFFFF0000.toInt())),
    )

private fun outcome(before: Int, after: Int) =
    LogOutcome(MapWithProgress(FiveCells, before), MapWithProgress(FiveCells, after))

/**
 * A frame every [FrameMillis] of virtual time; [background] makes the next frame come that much later,
 * like an app sent to the background.
 */
private class FakeFrameClock(private val scheduler: TestCoroutineScheduler) : MonotonicFrameClock {
    private var backgroundMillis = 0L

    fun background(millis: Long) {
        backgroundMillis += millis
    }

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        delay(FrameMillis)
        return onFrame((scheduler.currentTime + backgroundMillis) * 1_000_000)
    }
}

private class RecordingHaptics : HapticFeedback {
    val performed = mutableListOf<HapticFeedbackType>()

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        performed += hapticFeedbackType
    }
}

/** A [LogPlayback] on a Map whose state shows [filledCount] Cells filled until [stateShows] says otherwise. */
private class Harness(scope: TestScope, filledCount: Int, reducedMotion: Boolean = false) {
    val clock = FakeFrameClock(scope.testScheduler)
    val haptics = RecordingHaptics()
    private val stateFilledCount = mutableIntStateOf(filledCount)
    val playback =
        LogPlayback(
            scope = scope.backgroundScope + clock,
            haptics = haptics,
            isReducedMotion = { reducedMotion },
            filledCount = { stateFilledCount.intValue },
            graphite = Graphite,
        ).apply { screenSize = Size(1_000f, 2_000f) }

    /** What the Map shows now. */
    fun frame() =
        Frame(
            cascading = playback.stampClock != null,
            firstCellColour = playback.recolour?.colorOf(0, FlagRed),
            confetti = playback.confetti != null,
        )

    fun stateShows(filledCount: Int) {
        stateFilledCount.intValue = filledCount
        Snapshot.sendApplyNotifications()
    }
}

// Playback runs in the background scope, which advanceUntilIdle does not wait for.
@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.playThrough() {
    advanceTimeBy(10_000)
    runCurrent()
}

/** What the Map shows in one frame of playback. */
private data class Frame(
    val cascading: Boolean,
    // The first Cell's colour while the Map is held in pencil or turning into its flag; null when nothing holds it.
    val firstCellColour: Color?,
    val confetti: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.frames(harness: Harness, millis: Long = 10_000): List<Frame> =
    List((millis / FrameMillis).toInt()) {
        advanceTimeBy(FrameMillis)
        runCurrent()
        harness.frame()
    }

/** No playback: nothing stamps, nothing holds the Map, no confetti. */
private val Still = Frame(cascading = false, firstCellColour = null, confetti = false)

/** How many separate runs of frames match [predicate]. */
private fun List<Frame>.runsOf(predicate: (Frame) -> Boolean): Int =
    indices.count { predicate(this[it]) && ((it == 0) || !predicate(this[it - 1])) }

@OptIn(ExperimentalCoroutinesApi::class)
class LogPlaybackTest {
    @Test
    fun `the Cascade waits for the Map's state, then stamps the Cells the Log filled in fill order`() =
        runTest {
            val harness = Harness(this, filledCount = 1)

            harness.playback.play(outcome(before = 1, after = 4))
            advanceTimeBy(500)
            runCurrent()
            val stampClock = harness.playback.stampClock!!
            assertTrue("hidden until the state shows them", (1..3).all { stampClock.playTimeMillis(it) < 0 })
            assertEquals("a Cell filled before stays", CellStampMillis, stampClock.playTimeMillis(0))

            harness.stateShows(4)
            advanceTimeBy(FrameMillis * 4)
            runCurrent()
            val stamps = (1..3).map { stampClock.playTimeMillis(it) }
            assertTrue("the first Cell has started: $stamps", stamps.first() >= 0)
            assertEquals("each Cell starts after the one before: $stamps", stamps.sortedDescending(), stamps)

            playThrough()
            assertNull(harness.playback.stampClock)
            assertEquals(HapticFeedbackType.SegmentTick, harness.haptics.performed.first())
            assertEquals(HapticFeedbackType.Confirm, harness.haptics.performed.last())
        }

    @Test
    fun `a Log that brings Completion plays its Cascade, then the wave into the flag, then confetti once`() =
        runTest {
            val harness = Harness(this, filledCount = 3)

            harness.playback.play(outcome(before = 3, after = 5))
            runCurrent()
            assertEquals("held in pencil at once", Graphite, harness.playback.recolour?.colorOf(0, FlagRed))
            harness.stateShows(5)
            val frames = frames(harness)

            val lastCascading = frames.indexOfLast { it.cascading }
            val firstTurning = frames.indexOfFirst { (it.firstCellColour != null) && (it.firstCellColour != Graphite) }
            val lastHeld = frames.indexOfLast { it.firstCellColour != null }
            val firstConfetti = frames.indexOfFirst { it.confetti }
            val cascadeFrames = frames.take(lastCascading + 1)
            assertTrue("pencil through the Cascade", cascadeFrames.all { it.firstCellColour == Graphite })
            assertTrue("the wave starts after the Cascade", firstTurning > lastCascading)
            assertTrue("confetti fires as the wave ends", firstConfetti > lastHeld)
            assertEquals("confetti fires once", 1, frames.runsOf { it.confetti })
            assertEquals(Still, frames.last())
        }

    @Test
    fun `under reduced motion a Log that brings Completion only confirms with a haptic`() =
        runTest {
            val harness = Harness(this, filledCount = 3, reducedMotion = true)

            harness.playback.play(outcome(before = 3, after = 5))
            harness.stateShows(5)
            val frames = listOf(harness.frame()) + frames(harness)

            assertTrue("nothing moves: ${frames.distinct()}", frames.all { it == Still })
            assertEquals(listOf(HapticFeedbackType.Confirm), harness.haptics.performed)
        }

    @Test
    fun `a second Log mid-Cascade shows the first Log's Cells at once and plays its own`() =
        runTest {
            val harness = Harness(this, filledCount = 0)
            harness.playback.play(outcome(before = 0, after = 2))
            harness.stateShows(2)
            advanceTimeBy(FrameMillis * 2)
            runCurrent()

            harness.playback.play(outcome(before = 2, after = 4))
            harness.stateShows(4)
            advanceTimeBy(FrameMillis * 2)
            runCurrent()

            val stampClock = harness.playback.stampClock!!
            assertEquals(listOf(CellStampMillis, CellStampMillis), (0..1).map { stampClock.playTimeMillis(it) })
            assertTrue("the second Log stamps", stampClock.playTimeMillis(2) in 0 until CellStampMillis)
            playThrough()
            val confirms = harness.haptics.performed.count { it == HapticFeedbackType.Confirm }
            assertEquals("only the Cascade that ended confirms", 1, confirms)
        }

    @Test
    fun `time in the background never throws the Cascade ahead`() =
        runTest {
            val harness = Harness(this, filledCount = 0)
            harness.playback.play(outcome(before = 0, after = 5))
            harness.stateShows(5)
            advanceTimeBy(FrameMillis * 2)
            runCurrent()
            val stampClock = harness.playback.stampClock!!
            val beforeBackground = stampClock.playTimeMillis(0)

            harness.clock.background(5_000)
            advanceTimeBy(FrameMillis)
            runCurrent()

            assertTrue(
                "advanced ${stampClock.playTimeMillis(0) - beforeBackground} ms",
                stampClock.playTimeMillis(0) - beforeBackground <= MaxFrameMillis,
            )
            assertTrue("still playing", harness.playback.stampClock != null)
        }
}
