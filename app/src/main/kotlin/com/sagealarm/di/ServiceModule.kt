package com.sagealarm.di

import com.sagealarm.domain.scheduler.AlarmScheduler
import com.sagealarm.service.AlarmManagerScheduler
import com.sagealarm.service.AndroidTtsPlayer
import com.sagealarm.service.TtsPlayer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindTtsPlayer(impl: AndroidTtsPlayer): TtsPlayer

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmManagerScheduler): AlarmScheduler
}
