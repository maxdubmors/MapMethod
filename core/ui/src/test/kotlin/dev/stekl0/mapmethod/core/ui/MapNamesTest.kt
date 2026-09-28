package dev.stekl0.mapmethod.core.ui

import dev.stekl0.mapmethod.core.model.MapId
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class MapNamesTest {
    @Test
    fun `France is named by its identity`() {
        assertEquals(R.string.core_ui_map_name_france, mapNameRes(MapId("france")))
    }

    @Test
    fun `a Map without a name fails loudly`() {
        assertFailsWith<IllegalStateException> { mapNameRes(MapId("atlantis")) }
    }
}
