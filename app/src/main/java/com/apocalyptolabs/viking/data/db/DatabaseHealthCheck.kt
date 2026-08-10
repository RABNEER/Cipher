package com.apocalyptolabs.viking.data.db

import com.apocalyptolabs.viking.core.util.VikingLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseHealthCheck @Inject constructor(
    private val database: VikingDatabase
) {
    companion object {
        private const val TAG = "DatabaseHealthCheck"
    }

    suspend fun performCheck(): Boolean {
        return try {
            val cursor = database.openHelper.readableDatabase.query("SELECT count(*) FROM threat_logs")
            val isHealthy = cursor.moveToFirst()
            cursor.close()
            if (isHealthy) {
                VikingLogger.i("Database Health Check: PASSED. Table threat_logs is accessible.", TAG)
            } else {
                VikingLogger.w("Database Health Check: FAILED. Query returned empty cursor.", TAG)
            }
            isHealthy
        } catch (e: Exception) {
            VikingLogger.e("Database Health Check: FAILED with exception", e, TAG)
            false
        }
    }
}
