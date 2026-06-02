package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.data.remote.dto.SummaryReportDto
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TraccarApi {
    @GET("api/session")
    suspend fun getSession(): SessionDto

    @FormUrlEncoded
    @POST("api/session")
    suspend fun login(
        @Field("email") phoneNumber: String,
        @Field("password") password: String,
    ): SessionDto

    @DELETE("api/session")
    suspend fun logout()

    @GET("api/devices")
    suspend fun getDevices(): List<DeviceDto>

    @GET("api/positions")
    suspend fun getPositions(): List<PositionDto>

    @GET("api/reports/summary")
    suspend fun getSummaryReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("daily") daily: Boolean,
        @Query("deviceId") deviceId: Long,
    ): List<SummaryReportDto>
}
