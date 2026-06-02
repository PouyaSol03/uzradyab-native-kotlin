# Native Traccar Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first native Android foundation for the Uzradyab Traccar client with Room as source of truth, connected login/register/map UI, and a path to live WebSocket tracking.

**Architecture:** Replace the temporary starter structure with the requested clean architecture packages. Remote REST and WebSocket code writes to Room; repositories expose Room `Flow`; ViewModels shape screen state; Compose renders React-matching Persian RTL mobile UI.

**Tech Stack:** Kotlin, Jetpack Compose, MVVM/Clean Architecture, Hilt, Retrofit 3, OkHttp WebSocket, Room, WorkManager, Mapbox Maps SDK, Coroutines, Flow.

---

## Verified Dependency Inputs

- Room stable line: `androidx.room:room-*:2.8.4`.
- Hilt Android docs: `com.google.dagger:hilt-android:2.57.1` and Java 17.
- Retrofit Maven Central: `com.squareup.retrofit2:retrofit:3.0.0`.
- Mapbox Android install docs: `com.mapbox.maps:android-ndk27:11.24.3` and `com.mapbox.extension:maps-compose-ndk27:11.24.3`.

Do not run Gradle build/test tasks unless the user explicitly asks.

## React API Boundary

Before implementing a native screen, audit the matching React mobile component and add only the APIs that screen actually uses. The React project is a customized Traccar frontend, so keep service boundaries separate:

- Traccar server `https://app.uzradyab.ir`: session, devices, positions, reports, commands, geofences, WebSocket.
- Custom backend from `VITE_BACKEND_URL`: OTP, check-user, password helper, custom positions time-range helper.
- Payment server `https://pay.uzradyab.ir`: account charge list, pay, verify.
- Notification server `https://notification.uzradyab.ir`: notification preferences and latest event helpers.

For this first foundation, implement only Traccar session/devices/positions/socket and summary report distance. Add OTP/payment/notification APIs in later UI slices after auditing the exact React files.

## File Structure

Create or migrate toward this structure:

```text
app/src/main/java/com/example/uzradyab/
  UzradyabApplication.kt
  MainActivity.kt
  UzradyabApp.kt
  di/
    AppModule.kt
    DatabaseModule.kt
    NetworkModule.kt
    RepositoryModule.kt
    WorkerModule.kt
  domain/
    model/
      Device.kt
      Event.kt
      HistoryRetention.kt
      OfflineRegion.kt
      Position.kt
      TrackingConnectionState.kt
      UserSession.kt
    repository/
      AuthRepository.kt
      DeviceRepository.kt
      MapCacheRepository.kt
      PositionRepository.kt
      TrackingRepository.kt
    usecase/
      ObserveHomeSnapshotUseCase.kt
  data/
    remote/
      api/
        TraccarApi.kt
      dto/
        DeviceDto.kt
        EventDto.kt
        PositionDto.kt
        SessionDto.kt
        SummaryReportDto.kt
        SocketMessageDto.kt
      websocket/
        TraccarSocketClient.kt
    local/
      database/
        UzradyabDatabase.kt
      dao/
        DeviceDao.kt
        EventDao.kt
        PositionDao.kt
        UserSessionDao.kt
        OfflineRegionDao.kt
      entity/
        DeviceEntity.kt
        EventEntity.kt
        PositionEntity.kt
        UserSessionEntity.kt
        OfflineRegionEntity.kt
    mapper/
      DeviceMappers.kt
      EventMappers.kt
      PositionMappers.kt
      SessionMappers.kt
    repository/
      AuthRepositoryImpl.kt
      DeviceRepositoryImpl.kt
      MapCacheRepositoryImpl.kt
      PositionRepositoryImpl.kt
      TrackingRepositoryImpl.kt
  presentation/
    auth/
      AuthViewModel.kt
      LoginScreen.kt
      RegisterScreen.kt
    map/
      MapViewModel.kt
      HomeMapScreen.kt
      TrackingMap.kt
      SelectedDeviceStatusCard.kt
      DeviceListSheet.kt
      HomeBottomMenu.kt
    common/
      AppButton.kt
      AppInput.kt
      UiText.kt
  sync/
    worker/
      CacheCleanupWorker.kt
      FallbackPositionSyncWorker.kt
  map/
    offline/
      MapboxOfflineRegionManager.kt
```

Keep existing `ui/theme/*` if useful. Existing `core/*` and `feature/*` code can be removed after replacement files compile in the user's local build.

---

### Task 1: React Mobile API Audit For Foundation Slice

**Files:**
- Inspect: `/Users/pouyasoltani/Desktop/Projects/uzradyab/src/App.jsx`
- Inspect: `/Users/pouyasoltani/Desktop/Projects/uzradyab/src/SocketController.jsx`
- Inspect: `/Users/pouyasoltani/Desktop/Projects/uzradyab/src/login/LoginPage.jsx`
- Inspect: `/Users/pouyasoltani/Desktop/Projects/uzradyab/src/main/DeviceList.jsx`
- Inspect: `/Users/pouyasoltani/Desktop/Projects/uzradyab/src/common/components/StatusCard.jsx`

- [ ] **Step 1: List foundation Traccar calls from React**

Run:

```bash
rg -n "api/session|api/devices|api/positions|api/socket|api/reports/summary" \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/App.jsx \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/SocketController.jsx \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/login/LoginPage.jsx \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/main/DeviceList.jsx \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/common/components/StatusCard.jsx
```

Expected foundation allowlist:

```text
GET /api/session
POST /api/session
DELETE /api/session
GET /api/devices
GET /api/positions
WebSocket /api/socket
GET /api/reports/summary
```

- [ ] **Step 2: List secondary-service calls but do not implement them in this slice**

Run:

```bash
rg -n "VITE_BACKEND_URL|settings.secoundBackendUrl|VITE_NOTIFICATION_URL|notificationUrl|otp|pay|verify|accountChargeList" \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/login \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/settings \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/main \
  /Users/pouyasoltani/Desktop/Projects/uzradyab/src/common/components
```

Expected: OTP/custom helper calls belong to the custom backend, payment calls belong to `pay.uzradyab.ir`, and notification calls belong to `notification.uzradyab.ir`. Do not add these to `TraccarApi`.

- [ ] **Step 3: Confirm plan still matches audit**

Run:

```bash
rg -n "GET /api/session|POST /api/session|DELETE /api/session|GET /api/devices|GET /api/positions|GET /api/reports/summary|api/socket|VITE_BACKEND_URL|pay.uzradyab.ir|notification.uzradyab.ir" docs/superpowers/plans/2026-06-02-native-traccar-foundation.md
```

Expected: Traccar foundation endpoints are planned in Retrofit/WebSocket tasks; secondary-service APIs are documented as follow-up boundaries, not implemented.

- [ ] **Step 4: Commit audit amendment if this task changed the plan**

```bash
git add docs/superpowers/plans/2026-06-02-native-traccar-foundation.md docs/superpowers/specs/2026-06-02-native-traccar-foundation-design.md
git commit -m "docs: add react api audit boundary"
```

---

### Task 2: Gradle, Repositories, And Hilt Application Setup

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/uzradyab/UzradyabApplication.kt`

- [ ] **Step 1: Update repositories for Mapbox**

In `settings.gradle.kts`, add the Mapbox Maven repository inside `dependencyResolutionManagement.repositories`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}
```

- [ ] **Step 2: Add plugin and library versions**

Update `gradle/libs.versions.toml` with these entries:

```toml
[versions]
hilt = "2.57.1"
ksp = "2.0.21-1.0.28"
room = "2.8.4"
retrofit = "3.0.0"
gson = "2.13.2"
work = "2.11.0"
mapbox = "11.24.3"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.3.0" }
androidx-hilt-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.3.0" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
mapbox-android = { group = "com.mapbox.maps", name = "android-ndk27", version.ref = "mapbox" }
mapbox-compose = { group = "com.mapbox.extension", name = "maps-compose-ndk27", version.ref = "mapbox" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Keep the existing entries already present in the file.

- [ ] **Step 3: Apply root plugins**

Update root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 4: Apply app plugins and dependencies**

Update `app/build.gradle.kts` plugins and Java target:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

Add dependencies:

```kotlin
implementation(libs.hilt.android)
ksp(libs.hilt.compiler)
implementation(libs.androidx.hilt.work)
ksp(libs.androidx.hilt.compiler)
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
implementation(libs.retrofit)
implementation(libs.retrofit.converter.gson)
implementation(libs.gson)
implementation(libs.androidx.work.runtime.ktx)
implementation(libs.mapbox.android)
implementation(libs.mapbox.compose)
```

- [ ] **Step 5: Create Hilt application class**

Create `app/src/main/java/com/example/uzradyab/UzradyabApplication.kt`:

```kotlin
package com.example.uzradyab

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UzradyabApplication : Application()
```

- [ ] **Step 6: Register application and keep RTL support**

Update `<application>` in `app/src/main/AndroidManifest.xml`:

```xml
<application
    android:name=".UzradyabApplication"
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Uzradyab">
```

- [ ] **Step 7: Make MainActivity injectable**

Update `MainActivity.kt`:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UzradyabTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    UzradyabApp()
                }
            }
        }
    }
}
```

Add import:

```kotlin
import dagger.hilt.android.AndroidEntryPoint
```

- [ ] **Step 8: Static inspection**

Run only lightweight checks:

```bash
git diff -- settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/example/uzradyab/MainActivity.kt
```

Expected: dependency and Hilt setup only, no feature code.

- [ ] **Step 9: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/example/uzradyab/UzradyabApplication.kt app/src/main/java/com/example/uzradyab/MainActivity.kt
git commit -m "chore: add native architecture dependencies"
```

---

### Task 3: Domain Models And Repository Contracts

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/domain/model/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/domain/repository/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/domain/usecase/ObserveHomeSnapshotUseCase.kt`

- [ ] **Step 1: Create domain models**

Create `Device.kt`:

```kotlin
package com.example.uzradyab.domain.model

data class Device(
    val id: Long,
    val name: String,
    val uniqueId: String,
    val status: String,
    val category: String?,
    val disabled: Boolean,
    val lastUpdate: String?,
    val expirationTime: String?,
    val attributesJson: String,
)
```

Create `Position.kt`:

```kotlin
package com.example.uzradyab.domain.model

data class Position(
    val id: Long?,
    val deviceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val course: Double,
    val fixTime: String?,
    val serverTime: String?,
    val address: String?,
    val attributesJson: String,
)
```

Create `UserSession.kt`:

```kotlin
package com.example.uzradyab.domain.model

data class UserSession(
    val id: Long,
    val name: String,
    val email: String,
    val readonly: Boolean,
)
```

Create `Event.kt`:

```kotlin
package com.example.uzradyab.domain.model

data class Event(
    val id: Long,
    val deviceId: Long?,
    val type: String,
    val eventTime: String?,
    val attributesJson: String,
)
```

Create `OfflineRegion.kt`:

```kotlin
package com.example.uzradyab.domain.model

data class OfflineRegion(
    val id: String,
    val name: String,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long,
    val state: String,
)
```

Create `TrackingConnectionState.kt`:

```kotlin
package com.example.uzradyab.domain.model

enum class TrackingConnectionState {
    Idle,
    Connecting,
    Connected,
    Disconnected,
    PollingFallback,
    Unauthorized,
}
```

Create `HistoryRetention.kt`:

```kotlin
package com.example.uzradyab.domain.model

enum class HistoryRetention(val label: String, val maxAgeHours: Long?) {
    Last24Hours("24h", 24),
    Last72Hours("72h", 72),
    Last7Days("7d", 168),
    MaxRows("1000_rows", null),
}
```

- [ ] **Step 2: Create repository contracts**

Create `AuthRepository.kt`:

```kotlin
package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSession: Flow<UserSession?>
    suspend fun refreshSession(): Result<UserSession>
    suspend fun login(phoneNumber: String, password: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
}
```

Create `DeviceRepository.kt`:

```kotlin
package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    suspend fun refreshDevices(): Result<Unit>
}
```

Create `PositionRepository.kt`:

```kotlin
package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.Position
import kotlinx.coroutines.flow.Flow

interface PositionRepository {
    fun observeLatestPositions(): Flow<Map<Long, Position>>
    fun observeHistory(deviceId: Long, limit: Int): Flow<List<Position>>
    suspend fun refreshLatestPositions(): Result<Unit>
    suspend fun pruneHistory(maxRowsPerDevice: Int): Result<Unit>
}
```

Create `TrackingRepository.kt`:

```kotlin
package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.TrackingConnectionState
import kotlinx.coroutines.flow.StateFlow

interface TrackingRepository {
    val connectionState: StateFlow<TrackingConnectionState>
    fun start()
    fun stop()
    fun startFallbackPolling()
    fun stopFallbackPolling()
}
```

Create `MapCacheRepository.kt`:

```kotlin
package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.OfflineRegion
import kotlinx.coroutines.flow.Flow

interface MapCacheRepository {
    fun observeOfflineRegions(): Flow<List<OfflineRegion>>
    suspend fun clearOfflineRegions(): Result<Unit>
}
```

- [ ] **Step 3: Create home snapshot use case**

Create `ObserveHomeSnapshotUseCase.kt`:

```kotlin
package com.example.uzradyab.domain.usecase

import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class HomeSnapshot(
    val devices: List<Device>,
    val latestPositions: Map<Long, Position>,
)

class ObserveHomeSnapshotUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
) {
    operator fun invoke(): Flow<HomeSnapshot> {
        return combine(
            deviceRepository.observeDevices(),
            positionRepository.observeLatestPositions(),
        ) { devices, latestPositions ->
            HomeSnapshot(devices = devices, latestPositions = latestPositions)
        }
    }
}
```

- [ ] **Step 4: Static inspection**

```bash
rg -n "package com.example.uzradyab.domain" app/src/main/java/com/example/uzradyab/domain
```

Expected: every new domain file uses the `domain` package and has no Android framework imports.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/domain
git commit -m "feat: add domain contracts for tracking"
```

---

### Task 4: Room Source Of Truth

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/data/local/entity/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/local/dao/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/local/database/UzradyabDatabase.kt`
- Create: `app/src/main/java/com/example/uzradyab/di/DatabaseModule.kt`

- [ ] **Step 1: Create Room entities**

Create `DeviceEntity.kt`:

```kotlin
package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val uniqueId: String,
    val status: String,
    val category: String?,
    val disabled: Boolean,
    val lastUpdate: String?,
    val expirationTime: String?,
    val attributesJson: String,
)
```

Create `PositionEntity.kt`:

```kotlin
package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "positions",
    indices = [
        Index(value = ["deviceId", "serverTime"]),
        Index(value = ["deviceId", "isLatest"]),
    ],
)
data class PositionEntity(
    @PrimaryKey val localId: String,
    val remoteId: Long?,
    val deviceId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val course: Double,
    val fixTime: String?,
    val serverTime: String?,
    val address: String?,
    val attributesJson: String,
    val isLatest: Boolean,
)
```

Create `UserSessionEntity.kt`:

```kotlin
package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val singletonId: Int = 1,
    val id: Long,
    val name: String,
    val email: String,
    val readonly: Boolean,
)
```

Create `EventEntity.kt`:

```kotlin
package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Long,
    val deviceId: Long?,
    val type: String,
    val eventTime: String?,
    val attributesJson: String,
)
```

Create `OfflineRegionEntity.kt`:

```kotlin
package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_regions")
data class OfflineRegionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val minZoom: Double,
    val maxZoom: Double,
    val sizeBytes: Long,
    val state: String,
)
```

- [ ] **Step 2: Create DAOs**

Create `DeviceDao.kt`:

```kotlin
package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name COLLATE NOCASE")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Upsert
    suspend fun upsertAll(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices")
    suspend fun clear()
}
```

Create `PositionDao.kt`:

```kotlin
package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.PositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions WHERE isLatest = 1")
    fun observeLatestPositions(): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions WHERE deviceId = :deviceId ORDER BY serverTime DESC LIMIT :limit")
    fun observeHistory(deviceId: Long, limit: Int): Flow<List<PositionEntity>>

    @Query("UPDATE positions SET isLatest = 0 WHERE deviceId IN (:deviceIds)")
    suspend fun clearLatestFlags(deviceIds: List<Long>)

    @Upsert
    suspend fun upsertAll(positions: List<PositionEntity>)

    @Transaction
    suspend fun upsertLatest(positions: List<PositionEntity>) {
        val deviceIds = positions.map { it.deviceId }.distinct()
        if (deviceIds.isNotEmpty()) {
            clearLatestFlags(deviceIds)
        }
        upsertAll(positions.map { it.copy(isLatest = true) })
    }

    @Query(
        """
        DELETE FROM positions
        WHERE localId IN (
            SELECT localId FROM positions
            WHERE deviceId = :deviceId AND isLatest = 0
            ORDER BY serverTime DESC
            LIMIT -1 OFFSET :maxRows
        )
        """
    )
    suspend fun pruneDeviceHistory(deviceId: Long, maxRows: Int)

    @Query("SELECT DISTINCT deviceId FROM positions")
    suspend fun deviceIdsWithHistory(): List<Long>
}
```

Create `UserSessionDao.kt`:

```kotlin
package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_session WHERE singletonId = 1")
    fun observeCurrentSession(): Flow<UserSessionEntity?>

    @Upsert
    suspend fun upsert(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clear()
}
```

Create `EventDao.kt`:

```kotlin
package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY eventTime DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int): Flow<List<EventEntity>>

    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun clear()
}
```

Create `OfflineRegionDao.kt`:

```kotlin
package com.example.uzradyab.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.uzradyab.data.local.entity.OfflineRegionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineRegionDao {
    @Query("SELECT * FROM offline_regions ORDER BY name")
    fun observeRegions(): Flow<List<OfflineRegionEntity>>

    @Upsert
    suspend fun upsert(region: OfflineRegionEntity)

    @Query("DELETE FROM offline_regions")
    suspend fun clear()
}
```

- [ ] **Step 3: Create database**

Create `UzradyabDatabase.kt`:

```kotlin
package com.example.uzradyab.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.local.dao.EventDao
import com.example.uzradyab.data.local.dao.OfflineRegionDao
import com.example.uzradyab.data.local.dao.PositionDao
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.local.entity.DeviceEntity
import com.example.uzradyab.data.local.entity.EventEntity
import com.example.uzradyab.data.local.entity.OfflineRegionEntity
import com.example.uzradyab.data.local.entity.PositionEntity
import com.example.uzradyab.data.local.entity.UserSessionEntity

@Database(
    entities = [
        UserSessionEntity::class,
        DeviceEntity::class,
        PositionEntity::class,
        EventEntity::class,
        OfflineRegionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class UzradyabDatabase : RoomDatabase() {
    abstract fun userSessionDao(): UserSessionDao
    abstract fun deviceDao(): DeviceDao
    abstract fun positionDao(): PositionDao
    abstract fun eventDao(): EventDao
    abstract fun offlineRegionDao(): OfflineRegionDao
}
```

- [ ] **Step 4: Provide database with Hilt**

Create `DatabaseModule.kt`:

```kotlin
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

    @Provides fun provideUserSessionDao(database: UzradyabDatabase) = database.userSessionDao()
    @Provides fun provideDeviceDao(database: UzradyabDatabase) = database.deviceDao()
    @Provides fun providePositionDao(database: UzradyabDatabase) = database.positionDao()
    @Provides fun provideEventDao(database: UzradyabDatabase) = database.eventDao()
    @Provides fun provideOfflineRegionDao(database: UzradyabDatabase) = database.offlineRegionDao()
}
```

- [ ] **Step 5: Static inspection**

```bash
rg -n "@Entity|@Dao|@Database|observeLatestPositions|upsertLatest" app/src/main/java/com/example/uzradyab/data/local app/src/main/java/com/example/uzradyab/di/DatabaseModule.kt
```

Expected: Room annotations exist only in the data/local layer and database DI module.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/data/local app/src/main/java/com/example/uzradyab/di/DatabaseModule.kt
git commit -m "feat: add room source of truth"
```

---

### Task 5: Retrofit API, DTOs, Cookie Jar, And Mappers

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/data/remote/api/TraccarApi.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/remote/dto/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/mapper/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/di/NetworkModule.kt`
- Move or replace: `core/network/PersistentCookieJar.kt`

- [ ] **Step 1: Create Retrofit API**

Create `TraccarApi.kt`:

```kotlin
package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.data.remote.dto.SummaryReportDto
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TraccarApi {
    @GET("api/session")
    suspend fun getSession(): SessionDto

    @FormUrlEncoded
    @POST("api/session")
    suspend fun login(
        @Field("email") phoneNumber: String,
        @Field("password") password: String,
    ): SessionDto

    @DELETE("api/session")
    suspend fun logout()

    @GET("api/devices")
    suspend fun getDevices(): List<DeviceDto>

    @GET("api/positions")
    suspend fun getPositions(): List<PositionDto>

    @GET("api/reports/summary")
    suspend fun getSummaryReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("daily") daily: Boolean,
        @Query("deviceId") deviceId: Long,
    ): List<SummaryReportDto>
}
```

- [ ] **Step 2: Create DTOs**

Create DTOs with nullable fields to tolerate Traccar variations:

```kotlin
package com.example.uzradyab.data.remote.dto

data class SessionDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val readonly: Boolean = false,
)
```

```kotlin
package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class DeviceDto(
    val id: Long = 0,
    val name: String? = null,
    val uniqueId: String? = null,
    val status: String? = null,
    val category: String? = null,
    val disabled: Boolean = false,
    val lastUpdate: String? = null,
    val expirationTime: String? = null,
    val attributes: JsonObject? = null,
)
```

```kotlin
package com.example.uzradyab.data.remote.dto

import com.google.gson.JsonObject

data class PositionDto(
    val id: Long? = null,
    val deviceId: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Double = 0.0,
    val course: Double = 0.0,
    val fixTime: String? = null,
    val serverTime: String? = null,
    val address: String? = null,
    val attributes: JsonObject? = null,
)
```

```kotlin
package com.example.uzradyab.data.remote.dto

data class SummaryReportDto(
    val deviceId: Long = 0,
    val distance: Double = 0.0,
    val averageSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val startOdometer: Double = 0.0,
    val endOdometer: Double = 0.0,
)
```

- [ ] **Step 3: Create mappers**

Create `DeviceMappers.kt`:

```kotlin
package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.DeviceEntity
import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.domain.model.Device

fun DeviceDto.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name?.takeIf { it.isNotBlank() } ?: uniqueId.orEmpty(),
    uniqueId = uniqueId.orEmpty(),
    status = status ?: "unknown",
    category = category,
    disabled = disabled,
    lastUpdate = lastUpdate,
    expirationTime = expirationTime,
    attributesJson = attributes?.toString() ?: "{}",
)

fun DeviceEntity.toDomain(): Device = Device(
    id = id,
    name = name,
    uniqueId = uniqueId,
    status = status,
    category = category,
    disabled = disabled,
    lastUpdate = lastUpdate,
    expirationTime = expirationTime,
    attributesJson = attributesJson,
)
```

Create `PositionMappers.kt`:

```kotlin
package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.PositionEntity
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.domain.model.Position

fun PositionDto.toEntity(isLatest: Boolean): PositionEntity = PositionEntity(
    localId = id?.toString() ?: "${deviceId}_${serverTime ?: fixTime ?: latitude}_${longitude}",
    remoteId = id,
    deviceId = deviceId,
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    course = course,
    fixTime = fixTime,
    serverTime = serverTime,
    address = address,
    attributesJson = attributes?.toString() ?: "{}",
    isLatest = isLatest,
)

fun PositionEntity.toDomain(): Position = Position(
    id = remoteId,
    deviceId = deviceId,
    latitude = latitude,
    longitude = longitude,
    speed = speed,
    course = course,
    fixTime = fixTime,
    serverTime = serverTime,
    address = address,
    attributesJson = attributesJson,
)
```

Create `SessionMappers.kt`:

```kotlin
package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.UserSessionEntity
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.domain.model.UserSession

fun SessionDto.toEntity(): UserSessionEntity = UserSessionEntity(
    id = id,
    name = name?.takeIf { it.isNotBlank() } ?: email.orEmpty(),
    email = email.orEmpty(),
    readonly = readonly,
)

fun UserSessionEntity.toDomain(): UserSession = UserSession(
    id = id,
    name = name,
    email = email,
    readonly = readonly,
)
```

Create `EventMappers.kt`:

```kotlin
package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.local.entity.EventEntity
import com.example.uzradyab.data.remote.dto.EventDto
import com.example.uzradyab.domain.model.Event

fun EventDto.toEntity(): EventEntity = EventEntity(
    id = id,
    deviceId = deviceId,
    type = type,
    eventTime = eventTime,
    attributesJson = attributes?.toString() ?: "{}",
)

fun EventEntity.toDomain(): Event = Event(
    id = id,
    deviceId = deviceId,
    type = type,
    eventTime = eventTime,
    attributesJson = attributesJson,
)
```

- [ ] **Step 4: Provide network dependencies**

Create `NetworkModule.kt`:

```kotlin
package com.example.uzradyab.di

import android.content.Context
import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.data.remote.api.TraccarApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val DEFAULT_SERVER_URL = "https://app.uzradyab.ir/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): PersistentCookieJar {
        return PersistentCookieJar(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: PersistentCookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTraccarApi(retrofit: Retrofit): TraccarApi {
        return retrofit.create(TraccarApi::class.java)
    }
}
```

- [ ] **Step 5: Static inspection**

```bash
rg -n "api/session|api/devices|api/positions|api/reports/summary|DEFAULT_SERVER_URL|toEntity|toDomain" app/src/main/java/com/example/uzradyab/data app/src/main/java/com/example/uzradyab/di/NetworkModule.kt
```

Expected: endpoint strings only in `TraccarApi`, default server only in DI/network config, mapping code in `data/mapper`. No OTP, payment, or notification-service endpoints should appear in `TraccarApi`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/data/remote app/src/main/java/com/example/uzradyab/data/mapper app/src/main/java/com/example/uzradyab/di/NetworkModule.kt
git commit -m "feat: add traccar retrofit data layer"
```

---

### Task 6: Repository Implementations And DI Bindings

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/data/repository/*.kt`
- Create: `app/src/main/java/com/example/uzradyab/di/RepositoryModule.kt`

- [ ] **Step 1: Implement auth repository**

Create `AuthRepositoryImpl.kt`:

```kotlin
package com.example.uzradyab.data.repository

import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.UserSession
import com.example.uzradyab.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val userSessionDao: UserSessionDao,
    private val cookieJar: PersistentCookieJar,
) : AuthRepository {
    override val currentSession: Flow<UserSession?> =
        userSessionDao.observeCurrentSession().map { it?.toDomain() }

    override suspend fun refreshSession(): Result<UserSession> = runCatching {
        val session = api.getSession()
        userSessionDao.upsert(session.toEntity())
        session.toEntity().toDomain()
    }

    override suspend fun login(phoneNumber: String, password: String): Result<UserSession> = runCatching {
        val session = api.login(phoneNumber = phoneNumber, password = password)
        userSessionDao.upsert(session.toEntity())
        session.toEntity().toDomain()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        runCatching { api.logout() }
        cookieJar.clear()
        userSessionDao.clear()
    }
}
```

- [ ] **Step 2: Implement device and position repositories**

Create `DeviceRepositoryImpl.kt`:

```kotlin
package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.DeviceDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val deviceDao: DeviceDao,
) : DeviceRepository {
    override fun observeDevices(): Flow<List<Device>> {
        return deviceDao.observeDevices().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshDevices(): Result<Unit> = runCatching {
        deviceDao.upsertAll(api.getDevices().map { it.toEntity() })
    }
}
```

Create `PositionRepositoryImpl.kt`:

```kotlin
package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.PositionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.PositionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PositionRepositoryImpl @Inject constructor(
    private val api: TraccarApi,
    private val positionDao: PositionDao,
) : PositionRepository {
    override fun observeLatestPositions(): Flow<Map<Long, Position>> {
        return positionDao.observeLatestPositions()
            .map { rows -> rows.associate { it.deviceId to it.toDomain() } }
    }

    override fun observeHistory(deviceId: Long, limit: Int): Flow<List<Position>> {
        return positionDao.observeHistory(deviceId, limit)
            .map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun refreshLatestPositions(): Result<Unit> = runCatching {
        positionDao.upsertLatest(api.getPositions().map { it.toEntity(isLatest = true) })
        pruneHistory(maxRowsPerDevice = 1_000).getOrThrow()
    }

    override suspend fun pruneHistory(maxRowsPerDevice: Int): Result<Unit> = runCatching {
        positionDao.deviceIdsWithHistory().forEach { deviceId ->
            positionDao.pruneDeviceHistory(deviceId = deviceId, maxRows = maxRowsPerDevice)
        }
    }
}
```

- [ ] **Step 3: Bind repositories**

Create `RepositoryModule.kt`:

```kotlin
package com.example.uzradyab.di

import com.example.uzradyab.data.repository.AuthRepositoryImpl
import com.example.uzradyab.data.repository.DeviceRepositoryImpl
import com.example.uzradyab.data.repository.MapCacheRepositoryImpl
import com.example.uzradyab.data.repository.PositionRepositoryImpl
import com.example.uzradyab.data.repository.TrackingRepositoryImpl
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.MapCacheRepository
import com.example.uzradyab.domain.repository.PositionRepository
import com.example.uzradyab.domain.repository.TrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository
    @Binds @Singleton abstract fun bindPositionRepository(impl: PositionRepositoryImpl): PositionRepository
    @Binds @Singleton abstract fun bindTrackingRepository(impl: TrackingRepositoryImpl): TrackingRepository
    @Binds @Singleton abstract fun bindMapCacheRepository(impl: MapCacheRepositoryImpl): MapCacheRepository
}
```

- [ ] **Step 4: Static inspection**

```bash
rg -n "class .*RepositoryImpl|override fun observe|override suspend fun refresh|@Binds" app/src/main/java/com/example/uzradyab/data/repository app/src/main/java/com/example/uzradyab/di/RepositoryModule.kt
```

Expected: repository implementations bridge API and DAO, and DI binds every domain contract.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/data/repository app/src/main/java/com/example/uzradyab/di/RepositoryModule.kt
git commit -m "feat: connect repositories to room source of truth"
```

---

### Task 7: Auth UI Connected To ViewModel, With Login And Register Screens

**Files:**
- Modify: `app/src/main/java/com/example/uzradyab/UzradyabApp.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/auth/AuthViewModel.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/auth/LoginScreen.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/auth/RegisterScreen.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/common/AppButton.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/common/AppInput.kt`

- [ ] **Step 1: Create auth ViewModel**

Create `AuthViewModel.kt`:

```kotlin
package com.example.uzradyab.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val name: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val isSignedIn: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(value: String) {
        if (value.length <= 11 && value.all(Char::isDigit)) {
            _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.phoneNumber.length != 11 || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "شماره تلفن یا رمز عبور صحیح نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            authRepository.login(state.phoneNumber, state.password)
                .onSuccess {
                    deviceRepository.refreshDevices()
                    positionRepository.refreshLatestPositions()
                    _uiState.update { current -> current.copy(isSubmitting = false, isSignedIn = true) }
                }
                .onFailure {
                    _uiState.update { current ->
                        current.copy(
                            isSubmitting = false,
                            errorMessage = "ورود به برنامه با خطا مواجه شد",
                        )
                    }
                }
        }
    }

    fun registerVisualSubmit() {
        _uiState.update {
            it.copy(infoMessage = "ثبت‌نام در مرحله بعد به سرویس OTP متصل می‌شود")
        }
    }
}
```

- [ ] **Step 2: Create reusable auth controls**

Create `AppInput.kt` and `AppButton.kt` as Compose wrappers matching React mobile input/button dimensions, Persian text alignment, 8-12dp radius, and LTR override for phone/password input values.

- [ ] **Step 3: Implement login screen from React reference**

Create `LoginScreen.kt` with:

```kotlin
@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }
    LoginScreen(
        state = state,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick,
    )
}
```

Use the visual structure from React `LoginLayout.jsx` and `LoginPage.jsx`: background image treatment when assets are available, centered mobile form, Persian title, phone/password inputs, forgot password action, submit button, and register action.

- [ ] **Step 4: Implement register screen connected to ViewModel state**

Create `RegisterScreen.kt` with React-matching mobile fields:

- Name.
- Phone number.
- Password.
- Confirm password.
- Submit button.
- Link back to login.

For this foundation slice, submit calls `registerVisualSubmit()` and shows a Persian info message. Do not call a fake API. The screen is still connected to ViewModel state and ready for OTP service wiring.

Do not wire OTP or password-reset APIs in this task. React uses `VITE_BACKEND_URL` for those calls, not the Traccar API client.

- [ ] **Step 5: Update navigation**

Update `UzradyabApp.kt` routes:

```kotlin
private enum class AppRoute(val path: String) {
    Login("/signin"),
    Register("/register"),
    Home("/home"),
}
```

Use `LoginRoute`, `RegisterRoute`, and later `HomeMapRoute`.

- [ ] **Step 6: Static inspection**

```bash
rg -n "hiltViewModel|collectAsStateWithLifecycle|registerVisualSubmit|onSignedIn|/register" app/src/main/java/com/example/uzradyab/presentation/auth app/src/main/java/com/example/uzradyab/UzradyabApp.kt
```

Expected: auth screens use Hilt ViewModel and no direct network calls.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/presentation app/src/main/java/com/example/uzradyab/UzradyabApp.kt
git commit -m "feat: add connected auth screens"
```

---

### Task 8: First Map/Home UI From Room Flows

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/MapViewModel.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/HomeMapScreen.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/TrackingMap.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/DeviceListSheet.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/SelectedDeviceStatusCard.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/map/HomeBottomMenu.kt`
- Modify: `app/src/main/java/com/example/uzradyab/UzradyabApp.kt`

- [ ] **Step 1: Create map ViewModel**

Create `MapViewModel.kt`:

```kotlin
package com.example.uzradyab.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.model.TrackingConnectionState
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.TrackingRepository
import com.example.uzradyab.domain.usecase.ObserveHomeSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeMapUiState(
    val devices: List<Device> = emptyList(),
    val latestPositions: Map<Long, Position> = emptyMap(),
    val selectedDeviceId: Long? = null,
    val devicesOpen: Boolean = true,
    val connectionState: TrackingConnectionState = TrackingConnectionState.Idle,
    val signedOut: Boolean = false,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    observeHomeSnapshot: ObserveHomeSnapshotUseCase,
    private val authRepository: AuthRepository,
    private val trackingRepository: TrackingRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(HomeMapUiState())

    val uiState: StateFlow<HomeMapUiState> = combine(
        observeHomeSnapshot(),
        trackingRepository.connectionState,
        localState,
    ) { snapshot, connection, local ->
        val selected = local.selectedDeviceId ?: snapshot.devices.firstOrNull()?.id
        local.copy(
            devices = snapshot.devices,
            latestPositions = snapshot.latestPositions,
            selectedDeviceId = selected,
            connectionState = connection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeMapUiState())

    init {
        trackingRepository.start()
    }

    fun selectDevice(deviceId: Long) {
        localState.update { it.copy(selectedDeviceId = deviceId, devicesOpen = false) }
    }

    fun toggleDevices() {
        localState.update { it.copy(devicesOpen = !it.devicesOpen) }
    }

    fun logout() {
        viewModelScope.launch {
            trackingRepository.stop()
            authRepository.logout()
            localState.update { it.copy(signedOut = true) }
        }
    }
}
```

- [ ] **Step 2: Create map route and screen**

Create `HomeMapScreen.kt`:

```kotlin
@Composable
fun HomeMapRoute(
    onSignedOut: () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedOut) {
        if (state.signedOut) onSignedOut()
    }
    HomeMapScreen(
        state = state,
        onDeviceClick = viewModel::selectDevice,
        onToggleDevices = viewModel::toggleDevices,
        onLogoutClick = viewModel::logout,
    )
}
```

Build the screen to match React mobile `MainPage`: map fills the screen, toolbar overlays top, device list overlays when open, selected device status card anchors at the bottom.

- [ ] **Step 3: Add staged Mapbox composable boundary**

Create `TrackingMap.kt` with a real Mapbox composable when token is present and a graceful visual fallback when no token is configured. The fallback must look like a map surface, not a blank screen.

- [ ] **Step 4: Create React-matching device list and status card**

Create `DeviceListSheet.kt` using `LazyColumn` and 72dp rows, matching React `DeviceRow` status/name/last-update hierarchy.

Create `SelectedDeviceStatusCard.kt` matching React mobile `StatusCard`: centered bottom card, pull handle, device name, last update, GSM/GPS labels, primary manage-device button, share/directions icon buttons, and expiration warning when data exists.

The React `StatusCard` fetches `/api/reports/summary` for today distance. For this task, expose a placeholder today-distance field from ViewModel state unless `SummaryReportDto` has already been connected through a repository. Do not fetch summary directly from the composable.

- [ ] **Step 5: Create bottom menu**

Create `HomeBottomMenu.kt` matching React `BottomMenu`: rounded pill, RTL item ordering, map/devices/notifications/account actions as visible UI. Actions can update local state or navigate when routes exist.

- [ ] **Step 6: Wire home route**

Update `UzradyabApp.kt` to use `HomeMapRoute` for `/home`.

- [ ] **Step 7: Static inspection**

```bash
rg -n "HomeMapRoute|TrackingMap|SelectedDeviceStatusCard|DeviceListSheet|HomeBottomMenu|observeHomeSnapshot|latestPositions" app/src/main/java/com/example/uzradyab/presentation/map app/src/main/java/com/example/uzradyab/UzradyabApp.kt
```

Expected: home UI reads `HomeMapUiState`, not repositories or API clients directly.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/presentation/map app/src/main/java/com/example/uzradyab/UzradyabApp.kt
git commit -m "feat: add room-backed mobile map home"
```

---

### Task 9: WebSocket Live Updates And REST Fallback

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/data/remote/websocket/TraccarSocketClient.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/remote/dto/SocketMessageDto.kt`
- Modify: `app/src/main/java/com/example/uzradyab/data/repository/TrackingRepositoryImpl.kt`

- [ ] **Step 1: Create socket message DTO**

Create `SocketMessageDto.kt`:

```kotlin
package com.example.uzradyab.data.remote.dto

data class SocketMessageDto(
    val devices: List<DeviceDto>? = null,
    val positions: List<PositionDto>? = null,
    val events: List<EventDto>? = null,
)
```

- [ ] **Step 2: Create WebSocket client**

Create `TraccarSocketClient.kt`:

```kotlin
package com.example.uzradyab.data.remote.websocket

import com.example.uzradyab.data.remote.dto.SocketMessageDto
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

sealed interface SocketEvent {
    data object Opened : SocketEvent
    data class Message(val data: SocketMessageDto) : SocketEvent
    data class Closed(val code: Int, val reason: String) : SocketEvent
    data class Failed(val error: Throwable) : SocketEvent
}

class TraccarSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    fun connect(): Flow<SocketEvent> = callbackFlow {
        val request = Request.Builder()
            .url("wss://app.uzradyab.ir/api/socket")
            .build()
        val socket = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    trySend(SocketEvent.Opened)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    runCatching {
                        gson.fromJson(text, SocketMessageDto::class.java)
                    }.onSuccess { trySend(SocketEvent.Message(it)) }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    trySend(SocketEvent.Closed(code, reason))
                    close()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    trySend(SocketEvent.Failed(t))
                    close(t)
                }
            },
        )
        awaitClose { socket.close(4000, "client stopped") }
    }
}
```

- [ ] **Step 3: Implement tracking repository**

Implement `TrackingRepositoryImpl` so:

- `start()` launches a coroutine that collects socket events.
- `Opened` sets `Connected` and stops fallback polling.
- `Message` upserts devices, latest positions, and events into Room.
- `Failed` or unexpected `Closed` sets `Disconnected`, starts fallback polling, and reconnects with exponential backoff.
- `stop()` closes active collection and sets `Idle`.

- [ ] **Step 4: Static inspection**

```bash
rg -n "wss://app.uzradyab.ir/api/socket|PollingFallback|startFallbackPolling|stopFallbackPolling|upsertLatest|SocketEvent" app/src/main/java/com/example/uzradyab/data
```

Expected: WebSocket message handling writes through DAOs/repositories and does not update UI state directly.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/data/remote/websocket app/src/main/java/com/example/uzradyab/data/remote/dto/SocketMessageDto.kt app/src/main/java/com/example/uzradyab/data/repository/TrackingRepositoryImpl.kt
git commit -m "feat: add traccar websocket tracking"
```

---

### Task 10: WorkManager Sync And Cache Cleanup

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/sync/worker/FallbackPositionSyncWorker.kt`
- Create: `app/src/main/java/com/example/uzradyab/sync/worker/CacheCleanupWorker.kt`
- Create: `app/src/main/java/com/example/uzradyab/di/WorkerModule.kt`

- [ ] **Step 1: Create fallback worker**

Create `FallbackPositionSyncWorker.kt`:

```kotlin
package com.example.uzradyab.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class FallbackPositionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val positionRepository: PositionRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return positionRepository.refreshLatestPositions().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
```

- [ ] **Step 2: Create cleanup worker**

Create `CacheCleanupWorker.kt`:

```kotlin
package com.example.uzradyab.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.uzradyab.domain.repository.PositionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val positionRepository: PositionRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return positionRepository.pruneHistory(maxRowsPerDevice = 1_000).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }
}
```

- [ ] **Step 3: Add Hilt worker factory**

Update `UzradyabApplication.kt`:

```kotlin
package com.example.uzradyab

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UzradyabApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 4: Static inspection**

```bash
rg -n "@HiltWorker|FallbackPositionSyncWorker|CacheCleanupWorker|pruneHistory|refreshLatestPositions" app/src/main/java/com/example/uzradyab/sync app/src/main/java/com/example/uzradyab/UzradyabApplication.kt
```

Expected: workers call repository methods, not Retrofit/DAO directly.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/sync app/src/main/java/com/example/uzradyab/UzradyabApplication.kt app/src/main/java/com/example/uzradyab/di/WorkerModule.kt
git commit -m "feat: add sync and cache cleanup workers"
```

---

### Task 11: Mapbox Offline Boundary And Settings Entry

**Files:**
- Create: `app/src/main/java/com/example/uzradyab/map/offline/MapboxOfflineRegionManager.kt`
- Create: `app/src/main/java/com/example/uzradyab/data/repository/MapCacheRepositoryImpl.kt`
- Create: `app/src/main/java/com/example/uzradyab/presentation/settings/OfflineMapSettingsState.kt`

- [ ] **Step 1: Create offline manager boundary**

Create `MapboxOfflineRegionManager.kt`:

```kotlin
package com.example.uzradyab.map.offline

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxOfflineRegionManager @Inject constructor() {
    suspend fun clearOfflineMapData(): Result<Unit> {
        return runCatching { Unit }
    }
}
```

- [ ] **Step 2: Connect map cache repository**

Implement `MapCacheRepositoryImpl` so `observeOfflineRegions()` reads `OfflineRegionDao` and `clearOfflineRegions()` clears Room metadata and calls `MapboxOfflineRegionManager.clearOfflineMapData()`.

- [ ] **Step 3: Add settings-facing state contract**

Create `OfflineMapSettingsState.kt`:

```kotlin
package com.example.uzradyab.presentation.settings

import com.example.uzradyab.domain.model.OfflineRegion

data class OfflineMapSettingsState(
    val regions: List<OfflineRegion> = emptyList(),
    val storageLimitMb: Int = 500,
    val minZoom: Double = 5.0,
    val maxZoom: Double = 15.0,
    val isClearing: Boolean = false,
    val message: String? = null,
)
```

- [ ] **Step 4: Static inspection**

```bash
rg -n "MapboxOfflineRegionManager|clearOfflineRegions|OfflineRegionDao|500|5-15" app/src/main/java/com/example/uzradyab
```

Expected: offline map logic is behind repository/manager boundaries.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/uzradyab/map app/src/main/java/com/example/uzradyab/data/repository/MapCacheRepositoryImpl.kt app/src/main/java/com/example/uzradyab/presentation/settings/OfflineMapSettingsState.kt
git commit -m "feat: add map cache repository boundary"
```

---

### Task 12: Remove Temporary Starter Architecture

**Files:**
- Delete or migrate: `app/src/main/java/com/example/uzradyab/core/AppContainer.kt`
- Delete or migrate: `app/src/main/java/com/example/uzradyab/core/data/SessionRepository.kt`
- Delete or migrate: `app/src/main/java/com/example/uzradyab/core/network/TraccarApiClient.kt`
- Delete or migrate: old `feature/auth/signin/*`
- Delete or migrate: old `feature/home/*`

- [ ] **Step 1: Confirm new routes no longer use old ViewModels**

```bash
rg -n "feature\\.auth|feature\\.home|AppContainer|TraccarApiClient|SessionRepository\\(" app/src/main/java/com/example/uzradyab
```

Expected: no references from active app/navigation code.

- [ ] **Step 2: Delete superseded starter files**

Delete files only after references are gone. Keep `PersistentCookieJar` if `NetworkModule` still imports it, or move it to `data/remote`.

- [ ] **Step 3: Static inspection**

```bash
rg -n "com.example.uzradyab.core|com.example.uzradyab.feature" app/src/main/java/com/example/uzradyab
```

Expected: either no output or only retained design-system/theme code that is intentionally reused.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/uzradyab
git commit -m "refactor: remove temporary starter architecture"
```

---

### Task 13: Final Static Verification Handoff

**Files:**
- Inspect all changed files.

- [ ] **Step 1: Verify source-of-truth rule by search**

```bash
rg -n "TraccarApi|OkHttpClient|WebSocket|Dao|Repository" app/src/main/java/com/example/uzradyab/presentation
```

Expected: no `TraccarApi`, `OkHttpClient`, `WebSocket`, or DAO imports in presentation. Repository references should only appear in ViewModels if use cases are not yet introduced for that screen.

- [ ] **Step 2: Verify React UI references were used for first visible screens**

```bash
rg -n "StatusCard|BottomMenu|MainToolbar|LoginLayout|DeviceRow" docs/superpowers/specs/2026-06-02-native-traccar-foundation-design.md docs/superpowers/plans/2026-06-02-native-traccar-foundation.md
```

Expected: plan/spec explicitly name the React mobile components used as visual references.

- [ ] **Step 3: Verify no Gradle command was run**

Do not run Gradle. Ask the user to run Android Studio sync/build locally and send any errors.

- [ ] **Step 4: Final commit if cleanup changed docs or comments**

```bash
git status --short
git add docs/superpowers/plans/2026-06-02-native-traccar-foundation.md
git commit -m "docs: add native traccar implementation plan"
```
