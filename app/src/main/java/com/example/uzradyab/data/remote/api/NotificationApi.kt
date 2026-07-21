package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.NotificationPreferencesResponseDto
import com.example.uzradyab.data.remote.dto.FcmRegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationApi {
    @GET("handle_events/preferences/{userId}/")
    suspend fun getPreferences(
        @Path("userId") userId: Long
    ): NotificationPreferencesResponseDto

    @POST("handle_events/preferences/{userId}/toggle/{key}/")
    suspend fun togglePreference(
        @Path("userId") userId: Long,
        @Path("key") key: String
    )

    @POST("fcm/register/{userId}/")
    suspend fun registerFcmToken(
        @Path("userId") userId: String,
        @Body request: FcmRegisterRequestDto
    )
}
