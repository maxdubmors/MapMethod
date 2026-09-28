package dev.stekl0.mapmethod.core.data.catalogue

import dev.stekl0.mapmethod.core.model.MapDefinition
import dev.stekl0.mapmethod.core.model.MapId

/** Every Map the app offers, in the order it lists them. */
internal class MapCatalogue(
    val maps: List<MapDefinition>,
) {
    init {
        require(maps.distinctBy { it.id }.size == maps.size) { "Map identities repeat in $maps" }
    }

    operator fun get(id: MapId): MapDefinition =
        requireNotNull(maps.find { it.id == id }) { "No Map ${id.value} in the catalogue" }

    companion object {
        val Default: MapCatalogue = MapCatalogue(listOf(France))
    }
}
