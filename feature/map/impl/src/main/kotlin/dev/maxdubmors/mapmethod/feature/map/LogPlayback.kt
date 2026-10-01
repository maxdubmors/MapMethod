package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import dev.maxdubmors.mapmethod.core.designsystem.motion.isReducedMotion
import dev.maxdubmors.mapmethod.core.designsystem.theme.LocalNotebookPalette
import dev.maxdubmors.mapmethod.core.model.Flag
import dev.maxdubmors.mapmethod.core.model.LogOutcome
import dev.maxdubmors.mapmethod.core.ui.CellRecolour
import dev.maxdubmors.mapmethod.core.ui.CellStampMillis
import dev.maxdubmors.mapmethod.core.ui.StampClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

// Far enough in the past that every Cell waiting for its stamp is still hidden, or still graphite.
@Suppress("MayBeConst")
private val NotStarted = Long.MIN_VALUE / 2

/**
 * Plays what a Log did to the Map: the Cascade of the Cells it filled, with haptics, and when the Log
 * brought Completion, the one celebrated moment once the Cascade ends: a wave turns the Map from
 * graphite into its flag colours in fill order, and as it ends confetti fires once. The Map's state
 * alone already shows every filled Cell in its final colours, so playback that never runs leaves the
 * right static picture. Under reduced motion only the closing haptic plays.
 *
 * Cells are addressed by their position in fill order, which is also their index in the grid.
 */
@Stable
internal class LogPlayback(
    private val scope: CoroutineScope,
    private val haptics: HapticFeedback,
    private val isReducedMotion: () -> Boolean,
    private val filledCount: () -> Int,
    private val graphite: () -> Color,
) {
    /** When each Cell of the grid stamps; null when no Cascade plays. Read it only while drawing. */
    var stampClock: StampClock? by mutableStateOf(null)
        private set

    /**
     * Holds the filled Cells at graphite until the wave turns them; null when no Completion plays.
     * Read it only while drawing.
     */
    var recolour: CellRecolour? by mutableStateOf(null)
        private set

    /** The confetti on screen now; read it only while drawing, it changes every frame. */
    var confetti: Confetti? by mutableStateOf(null)
        private set

    /** The size of the screen the confetti falls across. Only the next burst reads it, so it is no state. */
    var screenSize: Size = Size.Zero

    // Read only while drawing, so each frame redraws the Map without recomposing it.
    private var cascadeElapsedMillis by mutableLongStateOf(NotStarted)
    private var waveElapsedMillis by mutableLongStateOf(NotStarted)
    private var playing: Job? = null

    /** Plays the Log's [outcome]; playback still running ends at once. */
    fun play(outcome: LogOutcome) {
        val filledCells = outcome.filledCells
        if (filledCells.isEmpty()) return
        val positions = outcome.before.filledCount until (outcome.before.filledCount + filledCells.size)
        val reducedMotion = isReducedMotion()
        val wave =
            if (outcome.completesMap && !reducedMotion) completionWave(outcome.after.cells.indices.toList()) else null
        // Set at once, so the Map, already complete in its state, stays graphite until the wave starts.
        val waveRecolour = wave?.let(::waveRecolour)
        if (waveRecolour != null) recolour = waveRecolour
        val previous = playing
        playing =
            scope.launch {
                previous?.cancelAndJoin()
                if (reducedMotion) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    return@launch
                }
                playCascade(cascadeSchedule(positions.toList()), awaitedFilledCount = outcome.after.filledCount)
                if ((wave != null) && (waveRecolour != null)) {
                    playCompletion(wave, waveRecolour, outcome.after.definition.flag)
                }
            }
    }

    private suspend fun playCascade(cascade: List<CascadeStamp>, awaitedFilledCount: Int) {
        stampClock = cascadeClock(cascade)
        try {
            // The event can arrive before the Map's state shows the Cells filled; until then they wait hidden.
            snapshotFlow { filledCount() }.first { it >= awaitedFilledCount }
            runCascade(cascade)
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        } finally {
            stampClock = null
            cascadeElapsedMillis = NotStarted
        }
    }

    private suspend fun playCompletion(wave: List<WaveRecolour>, waveRecolour: CellRecolour, flag: Flag) {
        try {
            playTimeline(wave.waveDurationMillis()) { elapsed -> waveElapsedMillis = elapsed }
        } finally {
            // A later Completion may already hold the Map; only this wave lets go of it.
            if (recolour === waveRecolour) recolour = null
            waveElapsedMillis = NotStarted
        }
        var current = confettiBurst(screenSize, confettiColors(flag), Random.Default)
        if (current.isOver) return
        confetti = current
        try {
            playFrames { frameMillis ->
                current = current.step(frameMillis)
                confetti = current
                !current.isOver
            }
        } finally {
            confetti = null
        }
    }

    private fun waveRecolour(wave: List<WaveRecolour>): CellRecolour =
        CellRecolour { index, color ->
            val delay = wave.getOrNull(index)?.delayMillis ?: 0L
            lerp(graphite(), color, recolourFraction(waveElapsedMillis - delay))
        }

    private fun cascadeClock(cascade: List<CascadeStamp>): StampClock {
        val first = cascade.first().position
        return StampClock { index ->
            val stamp = cascade.getOrNull(index - first)
            if (stamp == null) CellStampMillis else cascadeElapsedMillis - stamp.delayMillis
        }
    }

    private suspend fun runCascade(cascade: List<CascadeStamp>) {
        val duration = cascade.durationMillis()
        val ticks = cascade.filter { it.hasTick }.map { it.delayMillis }
        var nextTick = 0
        playTimeline(duration) { elapsed ->
            cascadeElapsedMillis = elapsed
            // Cells too close together to feel apart share one tick.
            if ((nextTick < ticks.size) && (ticks[nextTick] <= elapsed)) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                while ((nextTick < ticks.size) && (ticks[nextTick] <= elapsed)) nextTick++
            }
        }
    }
}

/** Calls [onElapsed] with the time played so far on each frame, from zero until [durationMillis] has played. */
private suspend fun playTimeline(durationMillis: Long, onElapsed: (elapsedMillis: Long) -> Unit) {
    var elapsed = 0L
    playFrames { frameMillis ->
        elapsed += frameMillis
        onElapsed(elapsed)
        elapsed < durationMillis
    }
}

/**
 * Calls [onFrame] with the length of each frame until it returns false, starting with a frame of
 * zero. Compose stops frames while the app is not visible, so the motion pauses with it; the first
 * frame back is clamped like any long frame, so time spent in the background never throws the
 * motion ahead or ends it early.
 */
private suspend fun playFrames(onFrame: (frameMillis: Long) -> Boolean) {
    var last = withFrameMillis { it }
    if (!onFrame(0L)) return
    do {
        val now = withFrameMillis { it }
        val frameMillis = (now - last).coerceIn(0L, MaxFrameMillis)
        last = now
    } while (onFrame(frameMillis))
}

@Composable
internal fun rememberLogPlayback(state: MapUiState): LogPlayback {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    // Read through state, so a theme change recolours the wave without dropping the playback that holds it.
    val graphite = rememberUpdatedState(LocalNotebookPalette.current.graphite)
    val currentState = rememberUpdatedState(state)
    return remember(scope, haptics) {
        LogPlayback(
            scope = scope,
            haptics = haptics,
            isReducedMotion = ::isReducedMotion,
            filledCount = { currentState.value.map.filledCount },
            graphite = { graphite.value },
        )
    }
}
