package com.apocalyptolabs.viking

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.apocalyptolabs.viking.core.util.AnrWatchdog
import com.apocalyptolabs.viking.core.util.GlobalExceptionHandler
import com.apocalyptolabs.viking.core.util.VikingLogger
import com.apocalyptolabs.viking.core.util.VikingReleaseTree
import com.apocalyptolabs.viking.data.db.DatabaseHealthCheck
import com.apocalyptolabs.viking.service.PermissionAuditWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class VikingApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var databaseHealthCheck: DatabaseHealthCheck

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 1. Install Global Exception Handler
        GlobalExceptionHandler.install(this)

        // 2. Initialize Timber with Viking release tree
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(VikingReleaseTree(this))
        }

        // 3. Start ANR Watchdog
        AnrWatchdog(3000L).start()

        // 4. Perform Startup Database Health Check
        CoroutineScope(Dispatchers.IO).launch {
            val isHealthy = databaseHealthCheck.performCheck()
            VikingLogger.i("Database Health Status: $isHealthy", "VikingApp")
        }

        // 5. Schedule weekly background audit with BATTERY_NOT_LOW constraint
        PermissionAuditWorker.scheduleWeeklyAudit(this)
    }
}
