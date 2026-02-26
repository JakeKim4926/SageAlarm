package com.sagealarm.di

import android.content.Context
import androidx.room.Room
import com.sagealarm.data.local.db.AlarmDao
import com.sagealarm.data.local.db.AlarmDatabase
import com.sagealarm.data.local.db.AlarmDatabase.Companion.MIGRATION_1_2
import com.sagealarm.data.local.db.AlarmDatabase.Companion.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(@ApplicationContext context: Context): AlarmDatabase =
        Room.databaseBuilder(
            context,
            AlarmDatabase::class.java,
            "sage_alarm.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao = database.alarmDao()
}
