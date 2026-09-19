package dev.stekl0.mapmethod.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cells")
public data class CellEntity(
    @PrimaryKey
    val orderIndex: Int,
    val row: Int,
    val col: Int,
    val filledAt: Long?,
)
