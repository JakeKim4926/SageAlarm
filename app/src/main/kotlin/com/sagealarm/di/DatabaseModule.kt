package com.sagealarm.di

import android.content.Context
import androidx.room.Room
import com.sagealarm.data.local.db.AlarmDao
import com.sagealarm.data.local.db.AlarmDatabase
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
        ).build()

    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao = database.alarmDao()
}
