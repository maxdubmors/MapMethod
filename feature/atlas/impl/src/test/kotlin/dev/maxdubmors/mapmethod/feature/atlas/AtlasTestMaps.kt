package dev.maxdubmors.mapmethod.feature.atlas

import dev.maxdubmors.mapmethod.core.data.catalogue.France
import dev.maxdubmors.mapmethod.core.model.MapId
import dev.maxdubmors.mapmethod.core.model.MapWithProgress

/** A second Map after France; the catalogue has no name for it, so the tests name it. */
internal val SecondMap = France.copy(id = MapId("second"))

/** An Atlas of two Maps: France with 3 Cells filled, then the second Map with 7. */
internal val TwoMapsAtlas =
    AtlasUiState(
        pages =
            listOf(
                AtlasPage(MapWithProgress(France, filledCount = 3)),
                AtlasPage(MapWithProgress(SecondMap, filledCount = 7), nameResOverride = android.R.string.unknownName),
            ),
    )
