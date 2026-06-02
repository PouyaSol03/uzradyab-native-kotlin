# Native Traccar Foundation Design

## Goal

Build the Kotlin Android app from the project brief as a native Traccar car tracking client. The React project at `/Users/pouyasoltani/Desktop/Projects/uzradyab` is the visual and interaction reference for mobile UI only. The native app must use the Kotlin architecture from the brief for data, state, caching, networking, and sync.

## Scope

This design covers the first complete foundation and vertical slice:

- Native architecture packages for presentation, domain, data, sync, map, and dependency injection.
- Login to the Traccar server using `POST /api/session`.
- Session persistence and logout.
- REST loading for current user, devices, and latest positions.
- Room cache for session, devices, latest positions, limited position history, events, and offline map regions.
- UI state read from Room `Flow`, never directly from API or WebSocket callbacks.
- WebSocket `/api/socket` as the primary live update channel.
- REST polling fallback only while WebSocket is disconnected.
- Mobile home/map experience that visually follows the React mobile app.

Full reports, settings detail screens, commands, geofences, notifications, payment, OTP registration, and complete offline self-hosted tile download UI are follow-up slices. The foundation must leave clean package boundaries for them.

## Source References

Use the React project only to understand what each mobile screen/component looks like and how the user moves through it.

Primary React references for the first slice:

- `src/login/LoginPage.jsx`
- `src/login/LoginLayout.jsx`
- `src/main/MainPage.jsx`
- `src/main/MainMap.jsx`
- `src/main/MainToolbar.jsx`
- `src/main/DeviceList.jsx`
- `src/main/DeviceRow.jsx`
- `src/common/components/StatusCard.jsx`
- `src/common/components/BottomMenu.jsx`
- `src/SocketController.jsx`

The React state model must not be copied. Redux, direct `fetch` calls in UI components, and browser-specific APIs are implementation details of the reference app, not native architecture guidance.

## React API Audit Rule

Before implementing each native screen, inspect the matching React mobile component for the exact APIs it calls. The native app should implement only the endpoints needed for that screen and keep each service in the correct boundary.

For the first foundation slice, the React mobile flow uses these Traccar endpoints:

- `GET /api/session`
- `POST /api/session`
- `DELETE /api/session`
- `POST /api/session/token` only for native/web token handoff in the React app; do not add it to Android until a native token flow is required.
- `GET /api/devices`
- `GET /api/devices/{id}` for device detail/payment verification follow-up screens.
- `PUT /api/devices/{id}` for device update/expiration follow-up screens.
- `GET /api/positions`
- `GET /api/positions?...` for replay/history follow-up screens.
- WebSocket `/api/socket`
- `GET /api/reports/summary?...` for today distance in `StatusCard` and report screens.

Secondary/custom services found in the React project must not be mixed into the Traccar API client:

- OTP and account helpers use `VITE_BACKEND_URL`, such as `/otp/send-otp/`, `/otp/verify-otp/`, `/api/traccar/check-user-exists/`, `/api/traccar/change-password/`, and `/api/traccar/positions/time-range/`.
- Payment and account charge flows use `https://pay.uzradyab.ir` through `settings.secoundBackendUrl`, such as `/accountChargeList/`, `/pay/`, and `/verify/`.
- Notification preferences/latest events use `https://notification.uzradyab.ir`, such as `/handle_events/preferences/...` and `/handle_events/latest/...`.

Do not add broad Traccar endpoints just because Traccar supports them. Add endpoint interfaces when the React-referenced native screen needs them.

## Architecture

Target package structure:

```text
app/src/main/java/com/example/uzradyab/
  presentation/
    auth/
    map/
    devices/
    history/
    settings/
  domain/
    model/
    repository/
    usecase/
  data/
    remote/
      api/
      websocket/
      dto/
    local/
      dao/
      entity/
      database/
    mapper/
    repository/
  sync/
    worker/
  map/
    offline/
  di/
```

The current `core/*` and `feature/*` code is starter code. It can be moved, replaced, or deleted as needed. Keep useful UI details from the existing sign-in screen only when they match the React mobile UI.

Dependency direction:

```text
presentation -> domain -> data
```

Compose screens depend on ViewModels and domain models. ViewModels depend on use cases or repositories. Repositories own remote/local coordination. Remote clients and DAOs are implementation details in `data`.

## Data Flow

Primary backend flow:

```text
GPS device -> Traccar Server -> Android App
```

Native app state flow:

```text
REST/WebSocket -> Repository -> Room Database -> Flow -> ViewModel -> Compose UI
```

Important rules:

- Compose must not call Retrofit, OkHttp WebSocket, DAOs, or workers directly.
- ViewModels must expose immutable UI state derived from Room flows.
- WebSocket and REST responses must be saved to Room before the UI reflects them.
- Cached data must appear immediately when the app opens.
- Network failures must not blank the UI if cached data exists.

## Libraries

Add the foundation libraries requested in the brief:

- Retrofit for Traccar REST API.
- OkHttp WebSocket for `/api/socket`.
- Room for local cache and source-of-truth flows.
- Hilt for dependency injection.
- WorkManager for retry/sync work that requires network.
- osmdroid with self-hosted OSM tiles from `https://map.exirfirm.com/tile/` for map rendering and offline map regions.
- Kotlin Coroutines and Flow for async and reactive state.

The existing handwritten OkHttp JSON client should be replaced by Retrofit APIs, except OkHttp remains the WebSocket transport and Retrofit's HTTP client.

## Domain Models

Create domain models for:

- `UserSession`
- `Device`
- `Position`
- `Event`
- `OfflineRegion`
- `TrackingConnectionState`
- `HistoryRetention`

Keep models small and focused. Position attributes can be stored as JSON text locally for the first slice, then parsed into typed helpers when a UI needs specific fields such as ignition, battery, alarm, GPS satellites, or GSM signal.

## Room Entities

Create these entities:

- `UserSessionEntity`: current logged-in user/session metadata.
- `DeviceEntity`: Traccar device data including id, name, unique id, status, category, disabled, last update, expiration time, and raw attributes JSON.
- `PositionEntity`: positions keyed by Traccar position id when available, with device id, coordinates, speed, course, times, address, and raw attributes JSON.
- `LatestPositionEntity` or a latest-position query strategy: latest position per device must be cheap to observe.
- `EventEntity`: latest relevant events from WebSocket/REST.
- `OfflineRegionEntity`: self-hosted OSM offline region metadata, size estimate, zoom range, and download state.
- `DailyDistanceEntity`: cached `/api/reports/summary` daily distance per device/date for React `StatusCard` parity.

Caching rules:

- Always cache latest position per device.
- Cache limited history only.
- Default max history is 1,000 positions per device.
- Add retention settings for 24h, 72h, and 7 days.
- Delete old position rows after inserts and during sync cleanup.
- Do not store unlimited GPS history.

## Remote API

Create Retrofit APIs for the Traccar server:

- `GET /api/session`
- `POST /api/session`
- `DELETE /api/session`
- `GET /api/devices`
- `GET /api/positions`
- `GET /api/reports/summary`

History, geofence, command, notification, payment, and OTP endpoints are follow-up slices. They should be added only after auditing their React mobile source component and identifying whether the call belongs to the Traccar server, `pay.uzradyab.ir`, `notification.uzradyab.ir`, or the custom backend from `VITE_BACKEND_URL`.

Use `https://app.uzradyab.ir` as the default server. Do not hardcode the server in screen code. Keep server configuration in data/di so custom Traccar servers can be supported later.

For sign-in, follow the React/native implementation note:

- Submit phone number as `email`.
- Submit password as `password`.
- Save cookies through OkHttp's cookie handling.

## WebSocket

Create an OkHttp WebSocket client for:

```text
wss://app.uzradyab.ir/api/socket
```

Connection flow:

1. Login succeeds.
2. Repository loads devices and latest positions using REST.
3. Repository saves REST results to Room.
4. Tracking repository opens WebSocket.
5. On `devices`, `positions`, or `events` messages, parse DTOs and upsert into Room.
6. Room flows update ViewModels and Compose.

WebSocket state rules:

- When WebSocket is active, no REST polling should run.
- When WebSocket disconnects unexpectedly, start reconnect with exponential backoff.
- While disconnected, start REST polling every 30-60 seconds.
- Stop fallback polling after WebSocket reconnects.
- Logout closes WebSocket intentionally and clears session/cookies/local session state.

## WorkManager

Use WorkManager only for background retry/sync that should run when network is available. It should not be the live tracking channel.

Initial workers:

- `FallbackPositionSyncWorker`: fetch latest positions when the app is offline/recovering or WebSocket is unavailable.
- `CacheCleanupWorker`: delete old history rows according to retention settings.

Workers write to Room through repositories or data-layer sync coordinators.

## Self-Hosted OSM Tiles

Self-hosted OSM tile integration is part of the target architecture. The app must not depend on Mapbox, Google Maps, public OSM tile servers, or a map download token. Map tiles should be fetched from the ExirFirm tile server by default:

```text
https://map.exirfirm.com/tile/{z}/{x}/{y}.png
```

- `TrackingMap` composable in presentation.
- Map state derived from latest position flows.
- Only latest marker per device for live view.
- Selected device marker follows React `MainMap`/`MapPositions` behavior.
- Tile source code in `map/tile`.
- Offline/cache boundary code in `map/offline`.

Offline map rules:

- User selects an area to download.
- Do not download a whole country at high zoom.
- Default zoom range is 5-15.
- Add storage limit setting, 500MB-2GB.
- Add clear offline map data action.

## UI And UX

The app is Persian and RTL by default. Preserve RTL layout from the React mobile UI. Force LTR only for phone numbers, passwords, plate numbers, coordinates, technical ids, and similar codes.

The Kotlin UI must match the React mobile app, screen by screen. For each screen implementation, first inspect the React component and then build reusable Compose components that reflect it.

First home/map slice should match these mobile behaviors:

- Top toolbar with menu and logo, based on React `MainToolbar` mobile state.
- Map-first screen, based on React `MainPage` and `MainMap`.
- Device list overlay behavior, based on `DeviceList` and `DeviceRow`.
- Auto-select first available device when no device is selected.
- Selecting a device hides the list on mobile when map-on-select behavior is enabled.
- Selected-device bottom status card, based on `StatusCard`.
- Bottom menu shape, item ordering, labels, and active state based on `BottomMenu`.
- Device status, last update, signal/GPS, expiration warning, share/directions actions, and manage-device menu should be added progressively as the required data lands in Room.

Do not redesign the UI. Any visual deviation should be caused by native platform constraints or an explicit user request.

## Presentation State

Recommended ViewModels:

- `AuthViewModel`
- `MapViewModel`
- `DeviceListViewModel`
- `HistoryViewModel`
- `SettingsViewModel`

For the first slice:

- `AuthViewModel` handles login form state, login action, and session observation.
- `MapViewModel` observes cached devices, latest positions, selected device id, connection state, and selected status-card data.
- Selection state can be persisted locally so reopening the app keeps the selected device when possible.

UI state should include loading/sync indicators, cached data availability, offline status, and user-facing errors. It should not include raw Retrofit responses.

## Repositories

Recommended repositories:

- `AuthRepository`
- `DeviceRepository`
- `PositionRepository`
- `TrackingRepository`
- `MapCacheRepository`

Responsibilities:

- `AuthRepository`: login, logout, session/cookie handling, current session flow.
- `DeviceRepository`: observe devices, refresh devices from REST, cache devices.
- `PositionRepository`: observe latest positions and limited history, refresh latest positions from REST, cache and prune history.
- `TrackingRepository`: coordinate WebSocket lifecycle, fallback polling, connection state, and live update persistence.
- `MapCacheRepository`: offline self-hosted OSM region metadata and commands.

## Error Handling

Error handling should favor cached continuity:

- If cached data exists, show it and surface a subtle offline/sync status.
- If login fails, keep the user on login with Persian error text.
- If session returns 401, clear local session and navigate to sign-in.
- If WebSocket disconnects, show cached data, update connection state, and start fallback polling.
- If REST fallback fails, retry with backoff and keep cached UI visible.

## Security

- Use HTTPS.
- Store cookies/session data outside UI code.
- Clear cookies and local session on logout.
- Do not expose server URL constants in composables.
- Keep a future path for encrypted storage if token/session data expands beyond cookies.

## Performance

Assume simple Android phones and around 5 devices.

- Observe only the data needed by each screen.
- Use limited Room queries for history.
- Do not render thousands of markers.
- Live map shows latest marker per device.
- History route should simplify or limit polylines when needed.
- Device list can use `LazyColumn`; virtualization is sufficient for expected device count.

## Testing Strategy

Follow TDD for behavior changes where practical.

High-value tests:

- Mapper tests for REST DTOs to entities/domain models.
- DAO tests for upsert and latest-position queries.
- Repository tests proving REST/WebSocket writes to Room and exposed flows read from Room.
- Tracking tests for WebSocket active means no polling, disconnected means fallback polling, reconnect stops polling.
- ViewModel tests proving UI state comes from repository flows and handles cached/offline states.

Gradle build/test tasks should not be run by default in this project unless the user explicitly asks. Lightweight static inspection is allowed.

## Migration Strategy

Because this is a test project, reshape the current code around the target architecture instead of preserving temporary structure.

Suggested migration order:

1. Add dependencies and Hilt application setup.
2. Add domain models and repository interfaces.
3. Add Room database, entities, DAOs, and mappers.
4. Add Retrofit API and cookie/session infrastructure.
5. Implement repositories with Room as the source of truth.
6. Rebuild login using the new `AuthViewModel`.
7. Implement initial home/map slice using Room flows.
8. Add WebSocket tracking updates.
9. Add fallback polling and cleanup workers.
10. Stage self-hosted OSM map and offline-region repository.

Each screen after the first slice should start by inspecting its React counterpart, then implementing reusable Compose components with the native architecture.
