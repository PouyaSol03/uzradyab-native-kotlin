package com.example.uzradyab.di

import com.example.uzradyab.data.repository.AuthRepositoryImpl
import com.example.uzradyab.data.repository.DeviceRepositoryImpl
import com.example.uzradyab.data.repository.EventRepositoryImpl
import com.example.uzradyab.data.repository.MapCacheRepositoryImpl
import com.example.uzradyab.data.repository.PositionRepositoryImpl
import com.example.uzradyab.data.repository.RegistrationRepositoryImpl
import com.example.uzradyab.data.repository.ReportRepositoryImpl
import com.example.uzradyab.data.repository.TrackingRepositoryImpl
import com.example.uzradyab.data.repository.AppConfigRepositoryImpl
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.EventRepository
import com.example.uzradyab.domain.repository.MapCacheRepository
import com.example.uzradyab.domain.repository.MapSettingsRepository
import com.example.uzradyab.domain.repository.NotificationRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.example.uzradyab.domain.repository.RegistrationRepository
import com.example.uzradyab.domain.repository.ReportRepository
import com.example.uzradyab.domain.repository.TokenRepository
import com.example.uzradyab.domain.repository.TrackingRepository
import com.example.uzradyab.domain.repository.AppConfigRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindPositionRepository(impl: PositionRepositoryImpl): PositionRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(
        trackingRepositoryImpl: TrackingRepositoryImpl
    ): TrackingRepository

    @Binds
    @Singleton
    abstract fun bindAppConfigRepository(
        appConfigRepositoryImpl: AppConfigRepositoryImpl
    ): AppConfigRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: com.example.uzradyab.data.repository.NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMapCacheRepository(impl: MapCacheRepositoryImpl): MapCacheRepository

    @Binds
    @Singleton
    abstract fun bindMapSettingsRepository(impl: com.example.uzradyab.data.repository.DefaultMapSettingsRepository): MapSettingsRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindRegistrationRepository(impl: RegistrationRepositoryImpl): RegistrationRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(impl: com.example.uzradyab.data.repository.TokenRepositoryImpl): TokenRepository

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(impl: com.example.uzradyab.data.repository.GeofenceRepositoryImpl): com.example.uzradyab.domain.repository.GeofenceRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: com.example.uzradyab.data.repository.ThemeRepositoryImpl): com.example.uzradyab.domain.repository.ThemeRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: com.example.uzradyab.data.repository.MaintenanceRepositoryImpl): com.example.uzradyab.domain.repository.MaintenanceRepository
}
