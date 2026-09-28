plugins {
    alias(libs.plugins.mapmethod.android.feature.api)
}

android {
    namespace = "dev.maxdubmors.mapmethod.feature.start.api"
}

dependencies {
    api(libs.kotlinx.serialization.core)
}
