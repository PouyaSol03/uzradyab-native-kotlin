package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.AppConfigDto
import retrofit2.http.GET

interface AppConfigApi {
    @GET("api/app-config/")
    suspend fun getAppConfig(): AppConfigDto
}
