plugins {
    alias(libs.plugins.mapmethod.android.library)
    alias(libs.plugins.mapmethod.android.room)
    alias(libs.plugins.mapmethod.hilt)
}

android {
    namespace = "dev.stekl0.mapmethod.core.database"
}
