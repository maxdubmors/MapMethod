plugins {
    alias(libs.plugins.mapmethod.android.feature.impl)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.stekl0.mapmethod.feature.start"
}

dependencies {
    implementation(project(":feature:start:api"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
}
