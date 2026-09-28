package dev.maxdubmors.mapmethod.feature.atlas.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The Atlas destination: the root once Start has been left, and where going back from a Map returns. */
@Serializable
public object AtlasNavKey : NavKey
