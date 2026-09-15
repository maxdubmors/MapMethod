package dev.stekl0.mapmethod

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.apply {
        buildFeatures.apply {
            compose = true
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            "implementation"(platform(bom))
            "androidTestImplementation"(platform(bom))
            "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        @Suppress("UnstableApiUsage")
        val rootProjectDirectory = isolated.rootProject.projectDirectory

        val buildDirectory =
            rootProjectDirectory
                .dir("build")
                .dir(projectDir.toRelativeString(rootDir))

        @Suppress("UnstableApiUsage")
        fun Provider<String>.outputDir(dir: String) =
            filter(String::toBoolean)
                .map { buildDirectory.dir(dir) }

        metricsDestination.set(
            providers.gradleProperty("enableComposeCompilerMetrics")
                .outputDir("compose-metrics"),
        )

        reportsDestination.set(
            providers.gradleProperty("enableComposeCompilerReports")
                .outputDir("compose-reports"),
        )

        stabilityConfigurationFiles.add(
            rootProjectDirectory.file("compose_compiler_config.conf"),
        )
    }
}
