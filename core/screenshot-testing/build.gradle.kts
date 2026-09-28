plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.maxdubmors.mapmethod.core.screenshottesting"
}

dependencies {
    api(libs.androidx.activity.compose)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.androidx.junit)
    api(libs.roborazzi)
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.robolectric)
}
