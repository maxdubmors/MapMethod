package dev.maxdubmors.mapmethod.core.model

/** One fillable unit of a Map at [row], [col] of its grid; its place in fill order is its place in the Map's Cells. */
public data class Cell(
    val row: Int,
    val col: Int,
)
