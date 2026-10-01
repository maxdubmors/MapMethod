package dev.maxdubmors.mapmethod.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MapDefinitionTest {
    private val flag = Flag(bands = listOf(0xFF000000.toInt()))

    @Test
    fun `a Map's grid spans its Cells from the first row and column`() {
        val map =
            MapDefinition(
                id = MapId("l"),
                cells = listOf(Cell(row = 0, col = 2), Cell(row = 3, col = 0)),
                flag = flag,
            )

        assertEquals(4, map.rows)
        assertEquals(3, map.cols)
    }

    @Test
    fun `a Map has at least one Cell`() {
        assertFailsWith<IllegalArgumentException> {
            MapDefinition(id = MapId("none"), cells = emptyList(), flag = flag)
        }
    }

    @Test
    fun `a Map's Cells lie on its grid, from row and column 0 on`() {
        assertFailsWith<IllegalArgumentException> {
            MapDefinition(id = MapId("north"), cells = listOf(Cell(row = -1, col = 0)), flag = flag)
        }
        assertFailsWith<IllegalArgumentException> {
            MapDefinition(id = MapId("west"), cells = listOf(Cell(row = 0, col = -1)), flag = flag)
        }
    }

    @Test
    fun `no two Cells of a Map share a place on the grid`() {
        assertFailsWith<IllegalArgumentException> {
            MapDefinition(
                id = MapId("twice"),
                cells = listOf(Cell(row = 1, col = 2), Cell(row = 1, col = 2)),
                flag = flag,
            )
        }
    }
}
