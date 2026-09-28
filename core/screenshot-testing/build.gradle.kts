plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.stekl0.mapmethod.core.screenshottesting"
}

dependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.roborazzi)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.material3)
}
