plugins {
    alias(libs.plugins.mapmethod.android.feature.impl)
}

android {
    namespace = "dev.stekl0.mapmethod.feature.second"
}

dependencies {
    implementation(project(":feature:second:api"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
}
