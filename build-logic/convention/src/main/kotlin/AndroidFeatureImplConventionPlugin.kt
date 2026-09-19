import dev.stekl0.mapmethod.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "mapmethod.android.library")
            apply(plugin = "mapmethod.android.library.compose")

            // ADR 0002: временному Second экрану не нужны ViewModel, Hilt и Orbit.
            // Владелец ViewModel со скоупом entry — когда появится настоящий ViewModel.
            dependencies {
                "implementation"(libs.findLibrary("androidx.navigation3.runtime").get())
            }
        }
    }
}
