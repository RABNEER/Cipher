package com.apocalyptolabs.viking.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threatLog: ThreatLogEntity): Long

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE severity IN ('HIGH', 'CRITICAL') ORDER BY timestamp DESC")
    fun getHighSeverityLogs(): Flow<List<ThreatLogEntity>>

    @Query("SELECT COUNT(*) FROM threat_logs WHERE severity IN ('HIGH', 'CRITICAL')")
    fun getActiveThreatCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM threat_logs")
    fun getTotalThreatCount(): Flow<Int>

    @Query("DELETE FROM threat_logs")
    suspend fun deleteAll()
}
