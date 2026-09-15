import com.android.build.api.dsl.ApplicationExtension
import dev.stekl0.mapmethod.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

abstract class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "com.autonomousapps.dependency-analysis")
            apply(plugin = "mapmethod.lint")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 37
                testOptions.animationsDisabled = true
            }
        }
    }
}
