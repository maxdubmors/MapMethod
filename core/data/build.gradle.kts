plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.hilt)
}

android {
    namespace = "dev.stekl0.mapmethod.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:database"))
    implementation(libs.kotlinx.coroutines.core)
    androidTestImplementation(testFixtures(project(":core:database")))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
