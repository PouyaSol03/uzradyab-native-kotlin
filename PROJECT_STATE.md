# Uzradyab Native Kotlin - Project State Summary

This document summarizes the current state, architecture, and recent changes of the `uzradyab-native-kotlin` project. You can provide this file to any external LLM (like Gemini Web) to give it immediate context on the project.

## Project Overview
- **Type**: Android Native App using **Jetpack Compose** and **Kotlin**.
- **Language/Direction**: Persian (RTL layout default).
- **Architecture**: Clean Architecture using **Hilt** (DI), **Coroutines/StateFlow**, **ViewModels**, and **Repositories**. Network client is **OkHttp/Retrofit** with persistent cookies.
- **Reference**: The app is a native port of an existing React web app (`Traccar`/`Vite`/`MUI`/`Tailwind`).

## API Endpoints
- **Main Traccar Backend**: `https://app.uzradyab.ir` (Endpoints: `/api/session`, `/api/devices`, `/api/positions`, `/api/reports/summary`, `/api/socket`).
- **Secondary Services**: 
  - OTP/Payment: `https://pay.uzradyab.ir`
  - Notifications: `https://notification.uzradyab.ir`
- **Auth**: Submit phone number (as `email`) + `password` to `POST /api/session`.

## Recently Completed Work

1. **In-App Network Debugging**:
   - Because Android LogCat was too noisy, we implemented a custom in-app live logger.
   - `AppLogger` singleton captures logs. `NetworkLogInterceptor` intercepts all OkHttp requests/responses (in `DEBUG` builds only).
   - Created `DebugLogScreen` to view these logs in real-time. Accessible via the "لاگ‌های شبکه [DEBUG]" button in the `AppMenuDialog` on the Home screen.
   - *Technical fix*: Resolved a Kotlin DSL scope conflict by moving the `ScrollToBottomFab` out of a nested `Box` to support `AnimatedVisibility`.

2. **Map Screen UI (`HomeMapScreen` & `DeviceManagementPanel`)**:
   - Fixed the RTL layout ordering inside `DeviceManagementPanel`. For example, `Arrangement.SpaceBetween` required reversing the order of items in code so they render correctly in RTL (e.g., text on right, buttons on left).
   - The "بازپخش مسیر" and "مشخصات دستگاه" buttons were styled to accurately match the Figma design (icons on proper sides).
   - Attached `DeviceManagementPanel` flush to the bottom of the screen (removed bottom padding) and added rounded top corners.
   - Built and added `AppBottomNavigation` (the floating pill-shaped menu with 4 tabs: Account, Map, Management, Alarms) overlapping the bottom of the map.

## Pending / Next Tasks

1. **Reports Screen Filtering**:
   - Need to add time filtering options (Today, Yesterday, Custom, etc.) in `ReportsScreen`, similar to the logic used in `MapTopControll`.
   
2. **Jalali Calendar Integration**:
   - The user requested a Jalali Calendar picker for the Custom date filter.
   - **Crucial Requirement**: The UI must show Jalali dates to the user, but the ViewModel/Repository logic **must convert and send standard Gregorian dates** to the backend API.

3. **Trip / Replay Screen Rewrite**:
   - Previous implementation was reverted because it didn't meet expectations.
   - Need to rebuild `TripScreen` matching the logic of `CombinedReportPage.jsx` and `MobileReplayPage.jsx` from the React project.
   - We must handle a known Gson parsing error (`JsonSyntaxException: Expected BEGIN_OBJECT but was BEGIN_ARRAY path $[0].route[0]`) from the combined report endpoint when selecting dates.

4. **WebSockets**:
   - Eventually, `/api/socket` needs to be implemented to provide real-time location updates.
