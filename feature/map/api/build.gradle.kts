plugins {
    alias(libs.plugins.mapmethod.android.feature.api)
}

android {
    namespace = "dev.maxdubmors.mapmethod.feature.map.api"
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.serialization.core)
}
