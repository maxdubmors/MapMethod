package dev.stekl0.mapmethod.core.database.dao

import dev.stekl0.mapmethod.core.database.model.CellEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [CellDao] mirroring IGNORE and single-fill semantics. */
internal class FakeCellDao(
    initial: List<CellEntity> = emptyList(),
) : CellDao {
    private val cells = MutableStateFlow(initial)
    var fillCalls: Int = 0
        private set

    override fun observeCells(): Flow<List<CellEntity>> = cells.map { it.sortedBy(CellEntity::orderIndex) }

    override suspend fun insertCells(cells: List<CellEntity>) {
        val known = this.cells.value.map { it.orderIndex }.toSet()
        this.cells.value = this.cells.value + cells.filter { it.orderIndex !in known }
    }

    override suspend fun fillNext(filledAt: Long): Int {
        fillCalls++
        val next = cells.value.filter { it.filledAt == null }.minByOrNull { it.orderIndex } ?: return 0
        cells.value =
            cells.value.map {
                if (it.orderIndex == next.orderIndex) it.copy(filledAt = filledAt) else it
            }
        return 1
    }
}
