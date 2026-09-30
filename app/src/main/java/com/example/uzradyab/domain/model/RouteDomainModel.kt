package com.example.uzradyab.domain.model

import org.maplibre.android.geometry.LatLng

data class RouteDomainModel(
    val points: List<LatLng>,
    val distanceText: String,
    val durationText: String,
    val summaryText: String,
    val distanceMeters: Long = 0,
    val durationSeconds: Long = 0,
    val steps: List<RouteStep> = emptyList()
)

data class RouteStep(
    val instruction: String,
    val streetName: String,
    val distanceMeters: Long,
    val durationSeconds: Long,
    val distanceText: String,
    val maneuver: Maneuver,
    val startLocation: LatLng,
    val bearingAfter: Int?,
    val rotaryExit: Int?,
    val rotaryName: String?,
    val points: List<LatLng>
)

data class Maneuver(
    /** Neshan/OSRM step type, e.g. "depart", "turn", "rotary", "exit rotary", "fork", "arrive". */
    val type: String,
    /** e.g. "left", "slight right", "straight", "uturn"; empty when not provided. */
    val modifier: String
)
