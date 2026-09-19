plugins {
    alias(libs.plugins.mapmethod.android.feature.impl)
    alias(libs.plugins.mapmethod.android.library.compose)
}

android {
    namespace = "dev.stekl0.mapmethod.feature.map"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":feature:map:api"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
}
