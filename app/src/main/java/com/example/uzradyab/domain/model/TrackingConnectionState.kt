package com.example.uzradyab.domain.model

enum class TrackingConnectionState {
    Idle,
    Connecting,
    Connected,
    Disconnected,
    PollingFallback,
    Unauthorized,
}
