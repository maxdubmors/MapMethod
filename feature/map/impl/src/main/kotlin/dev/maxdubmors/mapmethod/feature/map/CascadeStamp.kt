package dev.maxdubmors.mapmethod.feature.map

import dev.maxdubmors.mapmethod.core.ui.CellStampMillis

// The whole Cascade, last stamp included, fits this budget however many Cells a Log filled.
@Suppress("MayBeConst")
private val CascadeMaxMillis = 700L

@Suppress("MayBeConst")
private val MaxStaggerMillis = 60L

@Suppress("MayBeConst")
private val MaxTickedCells = 10

/**
 * One Cell of a Cascade.
 *
 * @property position the Cell's position in fill order.
 * @property delayMillis when the Cell starts stamping, counted from the start of the Cascade.
 * @property hasTick whether the Cell's stamp comes with a light haptic tick.
 */
internal data class CascadeStamp(
    val position: Int,
    val delayMillis: Long,
    val hasTick: Boolean,
)

/**
 * Stamps the Cells a Log filled one after another in fill order. A few Cells get a readable
 * stagger; with more Cells the stagger shrinks so the Cascade still ends within about 700 ms.
 * Only about the first ten Cells tick, so a large Log does not buzz for its whole length.
 */
internal fun cascadeSchedule(positions: List<Int>): List<CascadeStamp> {
    val ordered = positions.sorted()
    val gaps = (ordered.size - 1).coerceAtLeast(1).toLong()
    val span = minOf(MaxStaggerMillis * gaps, CascadeMaxMillis - CellStampMillis)
    return ordered.mapIndexed { step, position ->
        CascadeStamp(
            position = position,
            delayMillis = step * span / gaps,
            hasTick = step < MaxTickedCells,
        )
    }
}

/** When the Cascade ends: its last Cell has stamped. */
internal fun List<CascadeStamp>.durationMillis(): Long = if (isEmpty()) 0L else last().delayMillis + CellStampMillis
