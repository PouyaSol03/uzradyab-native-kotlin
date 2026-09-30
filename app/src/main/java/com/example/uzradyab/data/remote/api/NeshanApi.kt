package com.example.uzradyab.data.remote.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.example.uzradyab.BuildConfig

interface NeshanApi {
    @GET("v1/map-matching")
    suspend fun getMapMatching(
        @Query("path") path: String,
        @Header("Api-Key") apiKey: String = BuildConfig.NESHAN_SERVICE_API_KEY
    ): JsonObject

    @GET("v5/reverse")
    suspend fun getReverseGeocode(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Header("Api-Key") apiKey: String = BuildConfig.NESHAN_SERVICE_API_KEY
    ): JsonObject

    @GET("v4/direction")
    suspend fun getDirection(
        @Query("type") type: String,
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("alternative") alternative: Boolean = true,
        @Header("Api-Key") apiKey: String = BuildConfig.NESHAN_SERVICE_API_KEY
    ): com.example.uzradyab.data.remote.dto.NeshanDirectionResponse
}
