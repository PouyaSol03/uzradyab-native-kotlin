package com.example.uzradyab.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.local.dao.EventDao
import com.example.uzradyab.data.local.dao.DailyDistanceDao
import com.example.uzradyab.data.local.dao.OfflineRegionDao
import com.example.uzradyab.data.local.dao.PositionDao
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.local.entity.DailyDistanceEntity
import com.example.uzradyab.data.local.entity.DeviceEntity
import com.example.uzradyab.data.local.entity.EventEntity
import com.example.uzradyab.data.local.entity.OfflineRegionEntity
import com.example.uzradyab.data.local.entity.PositionEntity
import com.example.uzradyab.data.local.entity.UserSessionEntity
import com.example.uzradyab.data.local.entity.UserDeviceCrossRef

@Database(
    entities = [
        UserSessionEntity::class,
        DeviceEntity::class,
        PositionEntity::class,
        EventEntity::class,
        OfflineRegionEntity::class,
        DailyDistanceEntity::class,
        UserDeviceCrossRef::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class UzradyabDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun deviceDao(): DeviceDao
    abstract fun positionDao(): PositionDao
    abstract fun eventDao(): EventDao
    abstract fun offlineRegionDao(): OfflineRegionDao
    abstract fun dailyDistanceDao(): DailyDistanceDao
}
