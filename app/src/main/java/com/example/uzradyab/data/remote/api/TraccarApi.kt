package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.DeviceDto
import com.example.uzradyab.data.remote.dto.CreateUserRequestDto
import com.example.uzradyab.data.remote.dto.AddDeviceRequestDto
import com.example.uzradyab.data.remote.dto.PositionDto
import com.example.uzradyab.data.remote.dto.SessionDto
import com.example.uzradyab.data.remote.dto.SummaryReportDto
import com.example.uzradyab.data.remote.dto.CombinedReportDto
import com.example.uzradyab.data.remote.dto.EventDto
import com.example.uzradyab.data.remote.dto.GeofenceDto
import com.example.uzradyab.data.remote.dto.PermissionDto
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
    @GET("api/health")
    suspend fun checkServerHealth(): retrofit2.Response<Unit>

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

    @GET("api/devices")
    suspend fun getDeviceRaw(@Query("id") id: Long): retrofit2.Response<com.google.gson.JsonArray>

    @PUT("api/devices/{id}")
    suspend fun updateDeviceRaw(
        @Path("id") id: Long,
        @Body request: com.google.gson.JsonObject,
    ): retrofit2.Response<com.google.gson.JsonObject>

    @GET("api/positions")
    suspend fun getPositions(): List<PositionDto>

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/route")
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

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/stops")
    suspend fun getStopsReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("deviceId") deviceId: Long
    ): List<com.example.uzradyab.data.remote.dto.StopReportDto>

    @retrofit2.http.Headers("Accept: application/json")
    @GET("api/reports/trips")
    suspend fun getTripsReport(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("deviceId") deviceId: Long
    ): List<com.example.uzradyab.data.remote.dto.TripReportDto>

    @POST("api/commands/send")
    suspend fun sendCommand(
        @Body request: com.example.uzradyab.data.remote.dto.CommandRequestDto
    ): com.example.uzradyab.data.remote.dto.CommandResponseDto

    @GET("api/geofences")
    suspend fun getGeofences(@Query("deviceId") deviceId: Long? = null): List<GeofenceDto>

    @POST("api/geofences")
    suspend fun createGeofence(@Body geofence: GeofenceDto): GeofenceDto

    @PUT("api/geofences/{id}")
    suspend fun updateGeofence(@Path("id") id: Long, @Body geofence: GeofenceDto): GeofenceDto

    @DELETE("api/geofences/{id}")
    suspend fun deleteGeofence(@Path("id") id: Long)

    @POST("api/permissions")
    suspend fun linkPermission(@Body permission: PermissionDto)

    @retrofit2.http.HTTP(method = "DELETE", path = "api/permissions", hasBody = true)
    suspend fun unlinkPermission(@Body permission: PermissionDto)
}
