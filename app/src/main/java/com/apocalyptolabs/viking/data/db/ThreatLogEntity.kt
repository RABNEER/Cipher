package com.apocalyptolabs.viking.data.db

import android.os.Build
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apocalyptolabs.viking.core.model.Severity
import com.apocalyptolabs.viking.core.model.ThreatResult
import com.apocalyptolabs.viking.core.model.ThreatType

@Entity(tableName = "threat_logs")
data class ThreatLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val target: String,
    val type: String,
    val severity: String,
    val explanation: String,
    val action: String,
    val timestamp: Long,
    val appVersion: String = "1.0.0",
    val deviceApiLevel: Int = Build.VERSION.SDK_INT,
    val modelVersion: String = "gemma-270m-it-cpu-int4",
    val scanDurationMs: Long = 0L
) {
    fun toDomainModel(): ThreatResult {
        return ThreatResult(
            id = id,
            target = target,
            type = try { ThreatType.valueOf(type) } catch (e: Exception) { ThreatType.APK },
            severity = try { Severity.valueOf(severity) } catch (e: Exception) { Severity.MEDIUM },
            explanation = explanation,
            action = action,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomainModel(domain: ThreatResult, durationMs: Long = 0L): ThreatLogEntity {
            return ThreatLogEntity(
                id = domain.id,
                target = domain.target,
                type = domain.type.name,
                severity = domain.severity.name,
                explanation = domain.explanation,
                action = domain.action,
                timestamp = domain.timestamp,
                scanDurationMs = durationMs
            )
        }
    }
}
