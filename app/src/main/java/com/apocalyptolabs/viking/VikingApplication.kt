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

        GlobalExceptionHandler.install(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(VikingReleaseTree(this))
        }

        AnrWatchdog(3000L).start()

        CoroutineScope(Dispatchers.IO).launch {
            val isHealthy = databaseHealthCheck.performCheck()
            VikingLogger.i("Database Health Status: $isHealthy", "VikingApp")
        }

        PermissionAuditWorker.scheduleWeeklyAudit(this)
    }
}
