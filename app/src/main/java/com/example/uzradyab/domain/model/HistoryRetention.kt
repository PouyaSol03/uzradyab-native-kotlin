package com.example.uzradyab.domain.model

enum class HistoryRetention(val label: String, val maxAgeHours: Long?) {
    Last24Hours("24h", 24),
    Last72Hours("72h", 72),
    Last7Days("7d", 168),
    MaxRows("1000_rows", null),
}
