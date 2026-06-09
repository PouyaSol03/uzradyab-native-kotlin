package com.example.uzradyab.di

import com.example.uzradyab.data.repository.AuthRepositoryImpl
import com.example.uzradyab.data.repository.DeviceRepositoryImpl
import com.example.uzradyab.data.repository.EventRepositoryImpl
import com.example.uzradyab.data.repository.MapCacheRepositoryImpl
import com.example.uzradyab.data.repository.PositionRepositoryImpl
import com.example.uzradyab.data.repository.RegistrationRepositoryImpl
import com.example.uzradyab.data.repository.ReportRepositoryImpl
import com.example.uzradyab.data.repository.TrackingRepositoryImpl
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.EventRepository
import com.example.uzradyab.domain.repository.MapCacheRepository
import com.example.uzradyab.domain.repository.NotificationRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.example.uzradyab.domain.repository.RegistrationRepository
import com.example.uzradyab.domain.repository.ReportRepository
import com.example.uzradyab.domain.repository.TrackingRepository
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
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: com.example.uzradyab.data.repository.NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMapCacheRepository(impl: MapCacheRepositoryImpl): MapCacheRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindRegistrationRepository(impl: RegistrationRepositoryImpl): RegistrationRepository
}
