plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.stekl0.mapmethod.core.ui"
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.foundation)
}
