package dev.stekl0.mapmethod

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
import dev.stekl0.mapmethod.core.data.repository.MapRepository
import javax.inject.Inject

/**
 * [Application] class for Map Method
 */
@HiltAndroidApp
public class MapMethodApplication : Application() {
    // Injected at start, so Map progress is read while Start shows and the Map opens on it at once.
    @Inject
    internal lateinit var mapRepository: MapRepository

    override fun onCreate() {
        super.onCreate()

        setStrictModePolicy()
    }

    /**
     * Return true if the application is debuggable.
     */
    private fun isDebuggable(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * Enable all StrictMode thread and VM checks in debuggable builds.
     *
     * Violations are logged. Thread violations also flash the screen.
     */
    private fun setStrictModePolicy() {
        if (isDebuggable()) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build(),
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
        }
    }
}
