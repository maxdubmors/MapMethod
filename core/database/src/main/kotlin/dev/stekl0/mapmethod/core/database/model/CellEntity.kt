package dev.stekl0.mapmethod.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cells")
public data class CellEntity(
    @PrimaryKey
    val orderIndex: Int,
    val row: Int,
    val col: Int,
    val filledAt: Long?,
)
