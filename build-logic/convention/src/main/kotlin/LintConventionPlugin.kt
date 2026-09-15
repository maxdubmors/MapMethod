import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import dev.detekt.gradle.extensions.DetektExtension
import dev.stekl0.mapmethod.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class LintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jmailen.kotlinter")
            apply(plugin = "dev.detekt")

            pluginManager.withPlugin("com.android.base") {
                @Suppress("UnstableApiUsage")
                val detektConfig = isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml")
                configure<DetektExtension> {
                    config.setFrom(detektConfig)
                    buildUponDefaultConfig.set(true)
                }

                dependencies {
                    add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())
                    add("ktlint", libs.findLibrary("compose-rules-ktlint").get())
                }
            }

            pluginManager.withPlugin("com.android.application") {
                configure<ApplicationExtension> { lint(Lint::configure) }
            }

            pluginManager.withPlugin("com.android.library") {
                configure<LibraryExtension> { lint(Lint::configure) }
            }
        }
    }
}

private fun Lint.configure() {
    checkDependencies = true
    disable += "GradleDependency"
}
