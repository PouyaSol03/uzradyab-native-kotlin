package com.example.uzradyab.data.remote.dto

data class NeshanDirectionResponse(
    val routes: List<NeshanRoute>?
)

data class NeshanRoute(
    val overview_polyline: NeshanPolyline?,
    val legs: List<NeshanLeg>?
)

data class NeshanPolyline(
    val points: String?
)

data class NeshanLeg(
    val summary: String?,
    val distance: NeshanTextValue?,
    val duration: NeshanTextValue?,
    val steps: List<NeshanStep>?
)

data class NeshanStep(
    val name: String?,
    val instruction: String?,
    val bearing_after: Int?,
    val distance: NeshanTextValue?,
    val duration: NeshanTextValue?,
    val polyline: String?,
    // Neshan sends [lng, lat]
    val start_location: List<Double>?,
    val type: String?,
    val modifier: String?,
    val exit: Int?,
    val rotaryName: String?
)

data class NeshanTextValue(
    val text: String?,
    val value: Long?
)
