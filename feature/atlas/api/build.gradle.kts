plugins {
    alias(libs.plugins.mapmethod.android.feature.api)
}

android {
    namespace = "dev.maxdubmors.mapmethod.feature.atlas.api"
}

dependencies {
    api(libs.kotlinx.serialization.core)
}
