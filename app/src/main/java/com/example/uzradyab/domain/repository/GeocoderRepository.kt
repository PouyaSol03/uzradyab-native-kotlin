package com.example.uzradyab.domain.repository

interface GeocoderRepository {
    suspend fun getAddress(lat: Double, lon: Double): String
}