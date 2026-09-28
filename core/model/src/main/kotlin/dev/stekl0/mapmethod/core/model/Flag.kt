package dev.stekl0.mapmethod.core.model

/** A country's flag as vertical [bands] of ARGB colours, west to east; a Map takes it at Completion. */
public data class Flag(
    val bands: List<Int>,
) {
    init {
        require(bands.isNotEmpty()) { "A flag has at least one band" }
    }

    /** The ARGB colour of [col] among [cols] columns: equal bands, a remainder going to the western bands. */
    public fun colorAt(col: Int, cols: Int): Int = bands[(col * bands.size) / cols]
}
