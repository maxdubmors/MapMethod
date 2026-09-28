import com.android.build.api.dsl.LibraryExtension
import dev.stekl0.mapmethod.libs
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Screenshot tests on the JVM: Robolectric renders with native graphics, Roborazzi records and
 * verifies the baselines kept in `src/test/screenshots`. Cf. NiA's Roborazzi setup.
 */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "io.github.takahirom.roborazzi")

            extensions.configure<LibraryExtension> {
                testOptions.unitTests.isIncludeAndroidResources = true
                testOptions.unitTests.all {
                    it.systemProperty("robolectric.graphicsMode", "NATIVE")
                    // Robolectric reaches into FileDescriptor through JDK internals and loads native
                    // graphics, both of which recent JDKs only allow when asked.
                    it.jvmArgs(
                        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
                        "--enable-native-access=ALL-UNNAMED",
                    )
                    // A capture named "start_light.png" lands right in the output directory below.
                    it.systemProperty(
                        "roborazzi.record.filePathStrategy",
                        "relativePathFromRoborazziContextOutputDirectory",
                    )
                }
            }

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
            }

            dependencies {
                "testImplementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
                "testImplementation"(libs.findLibrary("androidx.compose.ui.test.junit4").get())
                "testRuntimeOnly"(libs.findLibrary("androidx.compose.ui.test.manifest").get())
                "testImplementation"(libs.findLibrary("robolectric").get())
                "testImplementation"(libs.findLibrary("roborazzi").get())
                // As in instrumented tests, Compose UI tests pull in an Espresso too old for recent
                // API levels (it calls the removed InputManager.getInstance), so raise it.
                constraints {
                    "testImplementation"(libs.findLibrary("androidx-espresso-core").get())
                }
            }
        }
    }
}
