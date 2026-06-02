package com.example.uzradyab.di

import android.content.Context
import androidx.room.Room
import com.example.uzradyab.data.local.database.UzradyabDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): UzradyabDatabase {
        return Room.databaseBuilder(
            context,
            UzradyabDatabase::class.java,
            "uzradyab.db",
        ).build()
    }

    @Provides
    fun provideUserSessionDao(database: UzradyabDatabase) = database.userSessionDao()

    @Provides
    fun provideDeviceDao(database: UzradyabDatabase) = database.deviceDao()

    @Provides
    fun providePositionDao(database: UzradyabDatabase) = database.positionDao()

    @Provides
    fun provideEventDao(database: UzradyabDatabase) = database.eventDao()

    @Provides
    fun provideOfflineRegionDao(database: UzradyabDatabase) = database.offlineRegionDao()
}
