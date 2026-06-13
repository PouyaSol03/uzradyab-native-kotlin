package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.data.remote.dto.CreateUserRequestDto
import com.example.uzradyab.data.remote.dto.AddDeviceRequestDto
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.data.remote.dto.SummaryReportDto
import com.example.uzradyab.data.remote.dto.CombinedReportDto
import com.example.uzradyab.data.remote.dto.EventDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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

    @POST("api/users")
    suspend fun createUser(@Body request: CreateUserRequestDto): SessionDto

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: SessionDto
    ): SessionDto

    @DELETE("api/session")
    suspend fun logout()

    @GET("api/devices")
    suspend fun getDevices(): List<DeviceDto>

    @POST("api/devices")
    suspend fun addDevice(@Body request: AddDeviceRequestDto): DeviceDto

    @PUT("api/devices/{id}")
    suspend fun updateDevice(
        @Path("id") id: Long,
        @Body request: AddDeviceRequestDto,
    ): DeviceDto

    @GET("api/positions")
    suspend fun getPositions(): List<PositionDto>

    @GET("api/positions")
    suspend fun getPositionsHistory(
        @Query("deviceId") deviceId: Long,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<PositionDto>

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/summary")
    suspend fun getSummaryReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("daily") daily: Boolean,
        @Query("deviceId") deviceId: Long,
    ): List<SummaryReportDto>

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/combined")
    suspend fun getCombinedReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("deviceId") deviceIds: List<Long>,
    ): List<CombinedReportDto>

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/events")
    suspend fun getEventsReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("deviceId") deviceId: Long,
        @Query("type") type: String
    ): List<EventDto>

    @POST("api/commands/send")
    suspend fun sendCommand(
        @Body request: com.example.uzradyab.data.remote.dto.CommandRequestDto
    ): com.example.uzradyab.data.remote.dto.CommandResponseDto
}
