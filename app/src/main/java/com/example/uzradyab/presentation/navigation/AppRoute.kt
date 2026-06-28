package com.example.uzradyab.presentation.navigation

enum class AppRoute(val path: String) {
    Startup("/startup"),
    Onboarding("/onboarding"),
    SignIn("/signin"),
    Register("/register"),
    Home("/home"),
    Devices("/devices"),
    Profile("/profile"),
    RenewCredit("/renew-credit"),
    Events("/events"),
    AddDevice("/add-device"),
    Reports("/reports"),
    DeviceStatus("/device-status"),
    DailyReport("/daily-report"),
    StopReports("/stop-reports"),
    ReplayTrip("/replay-trip"),
    CommandCenter("/command-center"),
    DebugLog("/debug-logs"),
    Geofence("/geofences"),
    TripReports("/trip-reports"),
}
