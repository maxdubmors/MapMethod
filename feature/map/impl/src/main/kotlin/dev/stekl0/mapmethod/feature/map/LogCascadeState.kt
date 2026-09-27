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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import dev.stekl0.mapmethod.core.designsystem.motion.isReducedMotion
import dev.stekl0.mapmethod.core.ui.CellStampMillis
import dev.stekl0.mapmethod.core.ui.StampClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Far enough in the past that every Cell of a cascade waiting to start is still hidden.
@Suppress("MayBeConst")
private val NotStarted = Long.MIN_VALUE / 2

// A Cell's delay when it is not part of the cascade; real delays are never negative.
@Suppress("MayBeConst")
private val NotStamping = -1L

/**
 * The Log cascade: the Cells a Log filled stamp onto the Map one after another in fill order, with
 * haptics. The Map's state alone still shows every filled Cell, so a cascade that never plays leaves
 * the right static picture. The camera is left where the user put it.
 */
@Stable
internal class LogCascadeState(
    private val scope: CoroutineScope,
    private val haptics: HapticFeedback,
    private val mapState: State<MapUiState>,
) {
    // The cascade playing now; empty when none is.
    private var schedule: List<CascadeStamp> by mutableStateOf(emptyList())

    // Read only while drawing, so each frame of the cascade redraws the Map without recomposing it.
    private var elapsedMillis by mutableLongStateOf(NotStarted)
    private var playing: Job? = null

    /** Tells the grid, indexed like [cells], when each Cell stamps; null when no cascade plays. */
    @Composable
    fun rememberStampClock(cells: List<CellUi>): StampClock? {
        val schedule = schedule
        return remember(cells, schedule) {
            if (schedule.isEmpty()) return@remember null
            val delayByOrderIndex = schedule.associate { it.orderIndex to it.delayMillis }
            // Indexed like the grid's Cells, so drawing a frame looks nothing up.
            val delays = LongArray(cells.size) { delayByOrderIndex[cells[it].orderIndex] ?: NotStamping }
            StampClock { index ->
                val delay = delays[index]
                if (delay == NotStamping) CellStampMillis else elapsedMillis - delay
            }
        }
    }

    /** Plays the cascade of the Cells a Log just filled; a cascade still playing ends at once. */
    fun play(orderIndexes: List<Int>) {
        val previous = playing
        playing =
            scope.launch {
                previous?.cancelAndJoin()
                if (orderIndexes.isEmpty()) return@launch
                if (isReducedMotion()) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    return@launch
                }
                val cascade = cascadeSchedule(orderIndexes)
                schedule = cascade
                try {
                    awaitFilled(cascade.last().orderIndex)
                    runFrames(cascade)
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                } finally {
                    schedule = emptyList()
                    elapsedMillis = NotStarted
                }
            }
    }

    /** Returns once the cascade playing now, if any, has ended. */
    suspend fun awaitEnd() {
        playing?.join()
    }

    // The event can arrive before the Map's state shows the Cells filled; until then they wait hidden.
    private suspend fun awaitFilled(lastOrderIndex: Int) {
        snapshotFlow { mapState.value }.first { state ->
            state.cells.any { it.orderIndex == lastOrderIndex && it.filled }
        }
    }

    private suspend fun runFrames(cascade: List<CascadeStamp>) {
        val duration = cascade.durationMillis()
        val ticks = cascade.filter { it.hasTick }.map { it.delayMillis }
        var nextTick = 0
        val start = withFrameMillis { it }
        var elapsed = 0L
        while (true) {
            elapsedMillis = elapsed
            // Cells too close together to feel apart share one tick.
            if ((nextTick < ticks.size) && (ticks[nextTick] <= elapsed)) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                while ((nextTick < ticks.size) && (ticks[nextTick] <= elapsed)) nextTick++
            }
            if (elapsed >= duration) return
            elapsed = withFrameMillis { it - start }
        }
    }
}

@Composable
internal fun rememberLogCascadeState(state: MapUiState): LogCascadeState {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val mapState = rememberUpdatedState(state)
    return remember(scope, haptics, mapState) { LogCascadeState(scope, haptics, mapState) }
}
