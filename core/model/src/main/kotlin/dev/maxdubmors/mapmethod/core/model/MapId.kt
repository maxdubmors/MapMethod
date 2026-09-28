package dev.maxdubmors.mapmethod.core.model

import kotlinx.serialization.Serializable

/** The stable identity of a Map: a slug such as `france`, never shown to the user. */
@Serializable
@JvmInline
public value class MapId(
    public val value: String,
)
