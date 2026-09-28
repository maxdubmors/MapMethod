package dev.maxdubmors.mapmethod.feature.start

import dev.maxdubmors.mapmethod.core.ui.CellStampMillis
import kotlin.math.PI
import kotlin.math.cos

// The whole assembly, last stamp included, whatever the number of filled Cells.
@Suppress("MayBeConst")
private val AssemblyMillis = 1_200L

/** How long one breath of the next preview Cell takes, faint to strongest and back. */
@Suppress("MayBeConst")
internal val BreathPeriodMillis = 2_400L

/** The strongest graphite fill of a breathing preview Cell: a hint of pencil, never a filled Cell. */
@Suppress("MayBeConst")
internal val MaxBreathAlpha = 0.3f

/**
 * The Start assembly: the first [count] Cells of the motif stamp onto the sheet one after another
 * in fill order, spread evenly so the last stamp ends at about 1.2 s. Returns each Cell's start
 * delay, counted from the start of the assembly.
 */
internal fun assemblySchedule(count: Int): List<Long> {
    val gaps = (count - 1).coerceAtLeast(1).toLong()
    val span = AssemblyMillis - CellStampMillis
    return List(count) { position -> position * span / gaps }
}

/** When the assembly ends: its last Cell has stamped. */
internal fun List<Long>.assemblyDurationMillis(): Long = if (isEmpty()) 0L else last() + CellStampMillis

/** Graphite fill of the breathing preview Cell, [millis] into its breathing: rises and falls smoothly. */
internal fun breathAlpha(millis: Long): Float {
    val phase = (millis % BreathPeriodMillis).toDouble() / BreathPeriodMillis
    return (MaxBreathAlpha * (1.0 - cos(2.0 * PI * phase)) / 2.0).toFloat()
}
