package dev.stekl0.mapmethod.feature.map

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewCellsTest {
    private fun cells(filled: List<Boolean>): List<CellUi> =
        filled.mapIndexed { index, isFilled ->
            CellUi(
                orderIndex = index,
                row = 0,
                col = index,
                filled = isFilled,
                isNext = index == filled.indexOfFirst { !it },
            )
        }

    @Test
    fun `count one previews single next cell`() {
        assertEquals(setOf(0), previewOrderIndexes(cells(listOf(false, false, false)), 1))
    }

    @Test
    fun `count previews that many next unfilled cells`() {
        assertEquals(setOf(0, 1, 2), previewOrderIndexes(cells(listOf(false, false, false)), 3))
    }

    @Test
    fun `preview skips filled cells in fill order`() {
        assertEquals(setOf(1, 2), previewOrderIndexes(cells(listOf(true, false, false)), 2))
    }

    @Test
    fun `null count falls back to single next cell`() {
        assertEquals(setOf(1), previewOrderIndexes(cells(listOf(true, false, false)), null))
    }

    @Test
    fun `full map previews nothing`() {
        assertEquals(emptySet<Int>(), previewOrderIndexes(cells(listOf(true, true)), 2))
        assertEquals(emptySet<Int>(), previewOrderIndexes(cells(listOf(true, true)), null))
    }

    @Test
    fun `count beyond remaining previews only what is left`() {
        assertEquals(setOf(1, 2), previewOrderIndexes(cells(listOf(true, false, false)), 99))
    }
}
