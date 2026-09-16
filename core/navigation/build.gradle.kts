plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.stekl0.mapmethod.core.navigation"
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.navigation3.runtime)
}
