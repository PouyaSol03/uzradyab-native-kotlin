package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.MapIrReverseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MapIrApi {
    @GET("reverse/fast-reverse")
    suspend fun getReverseGeocode(
        @Header("x-api-key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): MapIrReverseDto
}