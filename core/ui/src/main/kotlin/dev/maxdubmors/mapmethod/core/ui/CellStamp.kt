package dev.maxdubmors.mapmethod.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring

/**
 * How long a Cell takes to stamp onto the sheet: its fill springs up to full size with a slight
 * overshoot while fading in. The spring has settled by then, so it ends without a visible snap.
 */
@Suppress("MayBeConst")
public val CellStampMillis: Long = 240L

@Suppress("MayBeConst")
private val StampStartScale = 0.5f

@Suppress("MayBeConst")
private val StampFadeMillis = 150f

@Suppress("MayBeConst")
private val NanosPerMilli = 1_000_000L

// Damping 0.6 overshoots by about a tenth of the way travelled, so about 5% from half size.
private val StampSpring = FloatSpringSpec(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)

/** When each Cell stamps onto the sheet. Read while drawing, so a running cascade never recomposes. */
public fun interface StampClock {
    /**
     * Milliseconds since the Cell at [cellIndex] of the grid's cells started stamping: negative
     * before it starts, [CellStampMillis] or more once it has stamped.
     */
    public fun playTimeMillis(cellIndex: Int): Long
}

/** Scale of a stamping Cell's fill around its centre, [playTimeMillis] into its stamp. */
internal fun stampScale(playTimeMillis: Long): Float =
    when {
        playTimeMillis >= CellStampMillis -> 1f
        playTimeMillis <= 0L -> StampStartScale
        else -> StampSpring.getValueFromNanos(playTimeMillis * NanosPerMilli, StampStartScale, 1f, 0f)
    }

/** Opacity of a stamping Cell's fill, [playTimeMillis] into its stamp. */
internal fun stampAlpha(playTimeMillis: Long): Float =
    when {
        playTimeMillis >= CellStampMillis -> 1f
        playTimeMillis <= 0L -> 0f
        else -> FastOutSlowInEasing.transform((playTimeMillis / StampFadeMillis).coerceAtMost(1f))
    }
