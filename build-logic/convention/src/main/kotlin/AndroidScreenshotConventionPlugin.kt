import com.android.build.api.dsl.LibraryExtension
import dev.stekl0.mapmethod.libs
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Screenshot tests of an Android library on the JVM: Robolectric renders with native graphics,
 * Roborazzi records and verifies the baselines kept in `src/test/screenshots`, and
 * `:core:screenshot-testing` captures a screen in the app theme.
 */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "io.github.takahirom.roborazzi")

            extensions.configure<LibraryExtension> {
                testOptions.unitTests.isIncludeAndroidResources = true
                testOptions.unitTests.all {
                    it.systemProperty("robolectric.graphicsMode", "NATIVE")
                    // A capture's file name is taken relative to the output directory below.
                    it.systemProperty(
                        "roborazzi.record.filePathStrategy",
                        "relativePathFromRoborazziContextOutputDirectory",
                    )
                    // Robolectric reaches into FileDescriptor through JDK internals and loads native
                    // graphics, both of which recent JDKs only allow when asked.
                    it.jvmArgs(
                        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                        "--enable-native-access=ALL-UNNAMED",
                    )
                }
            }

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            }

            dependencies {
                "testImplementation"(project(":core:screenshot-testing"))
                "testImplementation"(platform(libs.findLibrary("androidx.compose.bom").get()))
                "testRuntimeOnly"(libs.findLibrary("androidx.compose.ui.test.manifest").get())
                "testImplementation"(libs.findLibrary("robolectric").get())
            }
        }
    }
}
