plugins {
    alias(libs.plugins.mapmethod.android.application)
    alias(libs.plugins.mapmethod.android.application.compose)
    alias(libs.plugins.mapmethod.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        applicationId = "dev.stekl0.mapmethod"
        versionCode = 3
        versionName = "0.2.0" // X.Y.Z; X = Major, Y = minor, Z = Patch level

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            optimization {
                enable = true
            }
        }
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "dev.stekl0.mapmethod"
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    implementation(project(":core:navigation"))
    implementation(project(":feature:map:api"))
    implementation(project(":feature:map:impl"))
    implementation(project(":feature:start:api"))
    implementation(project(":feature:start:impl"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
