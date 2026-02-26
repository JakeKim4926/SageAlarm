package com.sagealarm.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AlarmEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN isPuzzleEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarms ADD COLUMN alarmIntervalMinutes INTEGER NOT NULL DEFAULT 5")
                db.execSQL("ALTER TABLE alarms ADD COLUMN repeatCount INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE alarms ADD COLUMN isVibrationEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE alarms ADD COLUMN isTtsEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE alarms SET isTtsEnabled = 1 WHERE ttsMessage != ''")
                db.execSQL("ALTER TABLE alarms ADD COLUMN isMusicEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE alarms SET isMusicEnabled = 1 WHERE musicUri IS NOT NULL")
            }
        }
    }
}
