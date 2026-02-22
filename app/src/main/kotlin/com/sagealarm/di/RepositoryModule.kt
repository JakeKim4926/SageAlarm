package com.sagealarm.di

import com.sagealarm.data.repository.AlarmRepositoryImpl
import com.sagealarm.data.repository.AppSettingsRepositoryImpl
import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.domain.repository.AppSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository
}
