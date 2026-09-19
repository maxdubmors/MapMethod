package dev.stekl0.mapmethod.core.database.model

/** One fillable unit of the Map in fill order. */
public data class Cell(
    val orderIndex: Int,
    val row: Int,
    val col: Int,
    val filled: Boolean,
)

public fun CellEntity.toCell(): Cell =
    Cell(
        orderIndex = orderIndex,
        row = row,
        col = col,
        filled = filledAt != null,
    )
