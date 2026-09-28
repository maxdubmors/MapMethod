package dev.stekl0.mapmethod.core.model

/** The stable identity of a Map: a slug such as `france`, never shown to the user. */
@JvmInline
public value class MapId(
    public val value: String,
)
