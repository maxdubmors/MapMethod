package dev.stekl0.mapmethod.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import dev.stekl0.mapmethod.core.designsystem.motion.isReducedMotion
import dev.stekl0.mapmethod.core.model.Cell
import dev.stekl0.mapmethod.core.ui.CellRecolour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

// Far enough in the past that every Cell waiting for the wave is still graphite.
@Suppress("MayBeConst")
private val WaveNotStarted = Long.MIN_VALUE / 2

/**
 * Completion, the one celebrated moment: once the Log cascade ends, a wave turns the Map from
 * graphite into its flag colours in fill order, and as it ends confetti fires once. The Map's state
 * alone already shows the flag colours, so a Completion that never plays leaves the right picture.
 */
@Stable
internal class CompletionState(
    private val scope: CoroutineScope,
    private val mapState: State<MapUiState>,
) {
    // The wave playing now: set from the Completion event, so the Map stays graphite until it starts.
    private var wave: List<WaveRecolour>? by mutableStateOf(null)

    // Read only while drawing, so each frame of the wave redraws the Map without recomposing it.
    private var waveElapsedMillis by mutableLongStateOf(WaveNotStarted)

    /** The confetti on screen now; read it only while drawing, it changes every frame. */
    var confetti: Confetti? by mutableStateOf(null)
        private set

    /** The size of the screen the confetti falls across. Only the next burst reads it, so it is no state. */
    var screenSize: Size = Size.Zero

    private var playing: Job? = null

    /**
     * Holds the Map's filled Cells, indexed like [cells], at [graphite] until the wave turns them;
     * null when no wave plays.
     */
    @Composable
    fun rememberCellRecolour(cells: List<Cell>, graphite: Color): CellRecolour? {
        val wave = wave
        return remember(cells, wave, graphite) {
            if (wave == null) return@remember null
            val delayByOrderIndex = wave.associate { it.orderIndex to it.delayMillis }
            // Indexed like the grid's Cells, so drawing a frame looks nothing up.
            val delays = LongArray(cells.size) { delayByOrderIndex[cells[it].orderIndex] ?: 0L }
            CellRecolour { index, color ->
                lerp(graphite, color, recolourFraction(waveElapsedMillis - delays[index]))
            }
        }
    }

    /** Plays Completion once the Log [cascade] that completed the Map has ended. */
    fun play(cascade: LogCascadeState) {
        if (isReducedMotion()) return
        val wave = completionWave(mapState.value.cells.map { it.orderIndex })
        if (wave.isEmpty()) return
        playing?.cancel()
        this.wave = wave
        playing =
            scope.launch {
                try {
                    cascade.awaitEnd()
                    runWave(wave.waveDurationMillis())
                } finally {
                    this@CompletionState.wave = null
                    waveElapsedMillis = WaveNotStarted
                }
                fireConfetti()
            }
    }

    private suspend fun runWave(duration: Long) {
        var elapsed = 0L
        waveElapsedMillis = elapsed
        playFrames { frameMillis ->
            elapsed += frameMillis
            waveElapsedMillis = elapsed
            elapsed < duration
        }
    }

    private suspend fun fireConfetti() {
        var current = confettiBurst(screenSize, confettiColors(mapState.value.flag), Random.Default)
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
}

/**
 * Calls [onFrame] with the length of each frame until it returns false. Compose stops frames while
 * the app is not visible, so the motion pauses with it; the first frame back is clamped like any
 * long frame, so time spent in the background never throws the motion ahead or ends it early.
 */
private suspend fun playFrames(onFrame: (frameMillis: Long) -> Boolean) {
    var last = withFrameMillis { it }
    do {
        val now = withFrameMillis { it }
        val frameMillis = (now - last).coerceIn(0L, MaxFrameMillis)
        last = now
    } while (onFrame(frameMillis))
}

@Composable
internal fun rememberCompletionState(state: MapUiState): CompletionState {
    val scope = rememberCoroutineScope()
    val mapState = rememberUpdatedState(state)
    return remember(scope, mapState) { CompletionState(scope, mapState) }
}
