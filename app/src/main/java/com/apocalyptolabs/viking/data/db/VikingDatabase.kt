package com.apocalyptolabs.viking.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ThreatLogEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VikingDatabase : RoomDatabase() {
    abstract fun threatLogDao(): ThreatLogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE threat_logs ADD COLUMN appVersion TEXT NOT NULL DEFAULT '1.0.0'")
                db.execSQL("ALTER TABLE threat_logs ADD COLUMN deviceApiLevel INTEGER NOT NULL DEFAULT 26")
                db.execSQL("ALTER TABLE threat_logs ADD COLUMN modelVersion TEXT NOT NULL DEFAULT 'gemma-270m-it-cpu-int4'")
                db.execSQL("ALTER TABLE threat_logs ADD COLUMN scanDurationMs INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
