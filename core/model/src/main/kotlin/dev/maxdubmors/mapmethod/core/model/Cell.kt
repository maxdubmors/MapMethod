package dev.maxdubmors.mapmethod.core.model

/** One fillable unit of a Map at [row], [col] of its grid, filled at [orderIndex] in fill order. */
public data class Cell(
    val orderIndex: Int,
    val row: Int,
    val col: Int,
)
