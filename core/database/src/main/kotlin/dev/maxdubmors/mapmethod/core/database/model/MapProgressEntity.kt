package dev.maxdubmors.mapmethod.core.database.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** The progress of one Map: its first [filledCount] Cells in fill order are filled (ADR 0001). */
@Entity(tableName = "map_progress")
internal data class MapProgressEntity(
    @PrimaryKey
    val mapId: String,
    val filledCount: Int,
)
