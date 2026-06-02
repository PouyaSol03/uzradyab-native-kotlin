package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.OtpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthHelperApi {
    @POST("otp/send-otp/")
    suspend fun sendOtp(@Body request: OtpRequestDto)

    @POST("otp/verify-otp/")
    suspend fun verifyOtp(@Body request: OtpRequestDto)
}
