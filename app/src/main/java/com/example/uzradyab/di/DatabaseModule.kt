package com.example.uzradyab.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
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

    @Provides
    fun provideDailyDistanceDao(database: UzradyabDatabase) = database.dailyDistanceDao()

    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `daily_distance` (
                    `deviceId` INTEGER NOT NULL,
                    `date` TEXT NOT NULL,
                    `distanceMeters` REAL NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`deviceId`, `date`)
                )
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_session ADD COLUMN loginTimestamp INTEGER NOT NULL DEFAULT 0")
        }
    }
}
