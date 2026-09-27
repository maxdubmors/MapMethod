package dev.stekl0.mapmethod.feature.map

import androidx.compose.animation.core.FastOutSlowInEasing

// The whole wave, last recolour included, whatever the size of the Map.
@Suppress("MayBeConst")
private val WaveMillis = 1_000L

/** How long one Cell takes to turn from graphite into its flag colour. */
@Suppress("MayBeConst")
internal val CellRecolourMillis = 250L

/**
 * One Cell of the Completion wave.
 *
 * @property orderIndex the Cell's fill order index.
 * @property delayMillis when the Cell starts turning into its flag colour, counted from the start of the wave.
 */
internal data class WaveRecolour(
    val orderIndex: Int,
    val delayMillis: Long,
)

/**
 * The Completion wave: the Map's Cells turn from graphite into the flag colours one after another
 * in fill order, spread evenly over about one second.
 */
internal fun completionWave(orderIndexes: List<Int>): List<WaveRecolour> {
    val ordered = orderIndexes.sorted()
    val gaps = (ordered.size - 1).coerceAtLeast(1).toLong()
    val span = WaveMillis - CellRecolourMillis
    return ordered.mapIndexed { position, orderIndex ->
        WaveRecolour(orderIndex = orderIndex, delayMillis = position * span / gaps)
    }
}

/** When the wave ends: its last Cell has recoloured. */
internal fun List<WaveRecolour>.waveDurationMillis(): Long =
    if (isEmpty()) 0L else last().delayMillis + CellRecolourMillis

/** How far a Cell has turned from graphite into its flag colour, [millisIntoRecolour] into its recolour. */
internal fun recolourFraction(millisIntoRecolour: Long): Float =
    FastOutSlowInEasing.transform((millisIntoRecolour.toFloat() / CellRecolourMillis).coerceIn(0f, 1f))
