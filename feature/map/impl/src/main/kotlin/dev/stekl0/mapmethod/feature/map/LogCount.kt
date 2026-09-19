package dev.stekl0.mapmethod.feature.map

/** Parses the Log entry; null when empty, non-numeric, or below 1. Clamps to [remaining]. */
internal fun parseLogCount(text: String, remaining: Int): Int? {
    val value = text.toIntOrNull()
    return if ((value == null) || (value < 1)) null else value.coerceAtMost(remaining.coerceAtLeast(1))
}

/** Steps the Log entry; a cleared entry restarts from 1. Clamps to [remaining]. */
internal fun stepLogCount(current: Int?, step: Int, remaining: Int): Int {
    if (current == null) return 1
    return (current + step).coerceIn(1, remaining.coerceAtLeast(1))
}

/**
 * Clamps typed digits to a valid entry: empty clears, otherwise a clamped
 * 1-through-[remaining] value; invalid input keeps [current].
 */
internal fun clampEntryText(typed: String, current: String, remaining: Int): String =
    when {
        typed.isEmpty() -> {
            ""
        }

        remaining < 1 -> {
            current
        }

        else -> {
            val parsed = typed.toIntOrNull()
            if ((parsed == null) || (parsed < 1)) current else parsed.coerceAtMost(remaining).toString()
        }
    }
