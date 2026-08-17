# Uzradyab Native Kotlin - Jetpack Compose Client

This project is the native Android application for **Uzradyab**, built entirely with **Kotlin** and **Jetpack Compose**. It serves as the mobile client for the Traccar-based GPS tracking system, replacing web-based PWA views with high-performance native UI components.

The app defaults to **Persian (RTL)** layout and follows modern Android development practices.

---

## 🏗️ 1. Architecture & Code Organization

The project strictly follows **Clean Architecture** patterns, leveraging Android's recommended architecture guidelines. 

- **Dependency Injection**: Managed entirely by **Hilt** (`@HiltViewModel`, `@AndroidEntryPoint`, `@Module`).
- **Concurrency**: Asynchronous operations, state, and event flows are handled via **Coroutines** and **StateFlow/SharedFlow**.
- **UI State**: Uses the Unidirectional Data Flow (UDF) pattern. UI consumes `StateFlow` from ViewModels and dispatches intent/events back.

### Layer Separation
1.  **`domain`**: Contains the core business logic, Use Cases, and domain models (e.g., `Device`, `Position`, `User`). It has no dependencies on Android framework classes.
2.  **`data`**: Implements the repositories defined in the domain layer. Contains network models (DTOs), Retrofit service definitions, local Room databases (if applicable), and mapper classes that convert DTOs to Domain models.
3.  **`presentation`**: The UI layer containing Jetpack Compose screens, ViewModels, and UI-specific models. Organized by feature (e.g., `map`, `reports`, `auth`).
4.  **`di`**: Hilt modules defining how dependencies (network clients, repositories, use cases) are constructed and provided.

---

## 📡 2. Data & Network Layer (APIs)

The application communicates with multiple backend services. 
Networking is built on top of **OkHttp3** and **Retrofit2**.

### Main Endpoints (Traccar Backend)
**Base URL**: `https://app.uzradyab.ir`
*   **Authentication**: `POST /api/session` (Takes `email` (phone number) and `password`). 
*   **Session State**: `GET /api/session` (Validates if the user is still logged in).
*   **Devices & Positions**: `GET /api/devices`, `GET /api/positions`.
*   **Reports**: `GET /api/reports/summary`, `GET /api/reports/trips`, `GET /api/reports/stops`.
*   **WebSockets**: `/api/socket` (Planned/Implemented for live tracking).

### Secondary Microservices
*   **Payment & OTP**: `https://pay.uzradyab.ir`
*   **Notifications**: `https://notification.uzradyab.ir`

### Session Management & Security
The authentication is cookie-based. 
*   A custom **Persistent CookieJar** is attached to the OkHttp client.
*   Upon successful `POST /api/session`, the `JSESSIONID` cookie is stored locally.
*   The `SessionRepository` orchestrates the login, logout, and token refresh logic. 
*   Logging out triggers `DELETE /api/session` and clears the OkHttp cookie jar.

### Logging (In-App Debugger)
Due to standard LogCat being overly noisy, the app features an **In-App Network Debugger**:
*   `NetworkLogInterceptor`: Captures OkHttp request/response cycles (in `DEBUG` mode only).
*   `AppLogger`: Singleton that collects logs.
*   **DebugLogScreen**: A hidden screen accessible via the `AppMenuDialog` -> "لاگ‌های شبکه [DEBUG]" button that allows developers to see raw network requests live inside the app.

---

## 📱 3. Presentation Layer (Pages & UI Flows)

The UI is entirely written in Compose. It heavily emphasizes **customized components** to support Right-to-Left (RTL) alignments perfectly and match the Figma design system.

### Auth Flow (`presentation/auth`)
*   Starts at a splash/onboarding screen.
*   Users input their phone number. The API accepts this phone number mapped to the `email` field.
*   Supports both OTP flows (via the secondary payment/OTP service) and standard Password flows.

### Home & Map Flow (`presentation/map`)
*   **`HomeMapScreen.kt`**: The core shell of the app. It hosts the map view and heavily layers components on top of it.
*   **`TrackingMap.kt`**: The actual map SDK implementation (handling markers, polylines, camera movements).
*   **Overlays & Controls**: 
    *   `MapTopControls.kt` & `MapTopToolbar.kt`: Filter buttons, search, and top-screen navigation.
    *   `DeviceManagementPanel.kt`: A sophisticated bottom sheet anchored above the navigation bar, showing the currently selected device's stats, ignition state, and quick action buttons ("مشخصات دستگاه", "بازپخش مسیر"). It's highly optimized for RTL constraint layouts.
    *   `AppBottomNavigation.kt`: The floating pill-shaped menu (Account, Map, Management, Alarms).

### Replay / Trip Flow (`presentation/replay`)
*   **`ReplayMap.kt`**: Displays the historical path of a vehicle.
*   **Date Conversion**: A critical component of this flow is the **Jalali Calendar**. The UI must display Persian Jalali dates to the user, but the view models translate these dates into standard Gregorian timestamps before sending them to the `/api/reports/` endpoints.

### Reports (`presentation/reports`)
Contains individual screens for data analysis:
*   `DailyReportScreen.kt`
*   `TripReportsScreen.kt` (Lists trips with start/end coordinates).
*   `StopReportsScreen.kt` (Lists stops and their durations).
*   Features bottom sheets (e.g., `ColumnsSelectionBottomSheet`) for filtering visible data points.

### Settings & Management
*   **Alerts & Geofences** (`presentation/alerts`, `presentation/geofence`): CRUD operations for Traccar geofences.
*   **Commands** (`presentation/command`): Sending engine cut-off or custom commands to the GPS hardware.
*   **Profile** (`presentation/profile`): User account details, password resets, and session termination.

---

## 🛠️ 4. Development & Setup Guide

### Prerequisites
*   Android Studio Ladybug (or newer).
*   JDK 17.

### Local Configuration
You must configure your environment before building.
1.  **`local.properties`**: Ensure Android SDK path is set.
2.  **`.env` / `.env.example`**: If the build scripts utilize dot-env files for API keys, make sure to copy `.env.example` to `.env` and fill in any required Maps API keys. (Currently, the app connects directly to Traccar, so primary backend URLs are hardcoded or injected via BuildConfigs).

### Build Variants
*   **Debug**: Enables `NetworkLogInterceptor` and the in-app debug menu. 
*   **Release**: Strips logging, enables ProGuard/R8 minification.

### Key Internal Documents
If you are contributing to this project, please read the following markdown files located in the project root:
*   `AGENTS.md`: Specific rules regarding Compose RTL, Traccar API interactions, and UI component styling.
*   `PROJECT_STATE.md`: A live summary of recently completed tasks, bugs, and next immediate goals.
*   `PERFORMANCE_OPTIMIZATION_PLAN.md`: Guidelines on Compose recomposition limits and network caching.

---
