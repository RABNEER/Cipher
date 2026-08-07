package com.apocalyptolabs.viking.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.data.db.ThreatLogDao
import com.apocalyptolabs.viking.data.db.ThreatLogEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "viking_settings")

@Singleton
class ThreatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val threatLogDao: ThreatLogDao
) {
    companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_LAST_AUDIT_TIMESTAMP = longPreferencesKey("last_audit_timestamp")
        val KEY_TOTAL_THREATS_BLOCKED = intPreferencesKey("total_threats_blocked")

        // Module Toggles
        val KEY_MODULE_APK_ENABLED = booleanPreferencesKey("module_apk_enabled")
        val KEY_MODULE_UPI_ENABLED = booleanPreferencesKey("module_upi_enabled")
        val KEY_MODULE_SMS_ENABLED = booleanPreferencesKey("module_sms_enabled")
        val KEY_MODULE_CALL_ENABLED = booleanPreferencesKey("module_call_enabled")
        val KEY_MODULE_NFC_ENABLED = booleanPreferencesKey("module_nfc_enabled")
        val KEY_MODULE_PERM_ENABLED = booleanPreferencesKey("module_perm_enabled")
    }

    val allThreatLogs: Flow<List<ThreatResult>> = threatLogDao.getAllLogs().map { list ->
        list.map { it.toDomainModel() }
    }

    val highSeverityThreatLogs: Flow<List<ThreatResult>> = threatLogDao.getHighSeverityLogs().map { list ->
        list.map { it.toDomainModel() }
    }

    val activeThreatCount: Flow<Int> = threatLogDao.getActiveThreatCount()
    val totalThreatCount: Flow<Int> = threatLogDao.getTotalThreatCount()

    // DataStore settings flows
    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val lastAuditTimestamp: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_AUDIT_TIMESTAMP] ?: 0L
    }

    val totalThreatsBlocked: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_THREATS_BLOCKED] ?: 0
    }

    val moduleStatusMap: Flow<Map<String, Boolean>> = context.dataStore.data.map { prefs ->
        mapOf(
            "APK" to (prefs[KEY_MODULE_APK_ENABLED] ?: true),
            "UPI" to (prefs[KEY_MODULE_UPI_ENABLED] ?: true),
            "SMS" to (prefs[KEY_MODULE_SMS_ENABLED] ?: true),
            "CALL" to (prefs[KEY_MODULE_CALL_ENABLED] ?: true),
            "NFC" to (prefs[KEY_MODULE_NFC_ENABLED] ?: true),
            "PERM" to (prefs[KEY_MODULE_PERM_ENABLED] ?: true)
        )
    }

    suspend fun logThreat(result: ThreatResult, scanDurationMs: Long = 0L): Long {
        val entity = ThreatLogEntity.fromDomainModel(result, scanDurationMs)
        val id = threatLogDao.insert(entity)
        if (result.severity == com.apocalyptolabs.viking.core.model.Severity.HIGH ||
            result.severity == com.apocalyptolabs.viking.core.model.Severity.CRITICAL
        ) {
            incrementBlockedThreats()
        }
        return id
    }

    suspend fun clearAllLogs() {
        threatLogDao.deleteAll()
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun updateLastAuditTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_AUDIT_TIMESTAMP] = timestamp
        }
    }

    private suspend fun incrementBlockedThreats() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_TOTAL_THREATS_BLOCKED] ?: 0
            prefs[KEY_TOTAL_THREATS_BLOCKED] = current + 1
        }
    }

    suspend fun toggleModule(moduleKey: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (moduleKey) {
                "APK" -> prefs[KEY_MODULE_APK_ENABLED] = enabled
                "UPI" -> prefs[KEY_MODULE_UPI_ENABLED] = enabled
                "SMS" -> prefs[KEY_MODULE_SMS_ENABLED] = enabled
                "CALL" -> prefs[KEY_MODULE_CALL_ENABLED] = enabled
                "NFC" -> prefs[KEY_MODULE_NFC_ENABLED] = enabled
                "PERM" -> prefs[KEY_MODULE_PERM_ENABLED] = enabled
            }
        }
    }
}
