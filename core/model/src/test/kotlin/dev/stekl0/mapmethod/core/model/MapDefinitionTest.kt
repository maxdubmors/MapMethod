package dev.stekl0.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MapDefinitionTest {
    private val flag = Flag(bands = listOf(0xFF000000.toInt()))

    @Test
    fun `a Map's grid spans its Cells from the first row and column`() {
        val map =
            MapDefinition(
                id = MapId("l"),
                cells = listOf(Cell(orderIndex = 0, row = 0, col = 2), Cell(orderIndex = 1, row = 3, col = 0)),
                flag = flag,
            )

        assertEquals(4, map.rows)
        assertEquals(3, map.cols)
    }

    @Test
    fun `a Map without Cells has no grid`() {
        val map = MapDefinition(id = MapId("none"), cells = emptyList(), flag = flag)

        assertEquals(0, map.rows)
        assertEquals(0, map.cols)
    }
}
