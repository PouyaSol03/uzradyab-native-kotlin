package com.example.uzradyab.data.remote.api

import com.example.uzradyab.data.remote.dto.OtpRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthHelperApi {
    @POST("otp/send-otp/")
    suspend fun sendOtp(@Body request: OtpRequestDto)

    @POST("otp/verify-otp/")
    suspend fun verifyOtp(@Body request: OtpRequestDto)

    @retrofit2.http.GET("accountChargeList/")
    suspend fun getAccountChargeList(): List<com.example.uzradyab.data.remote.dto.AccountChargeDto>

    @POST("pay/")
    suspend fun pay(@Body request: com.example.uzradyab.data.remote.dto.PaymentRequestDto): com.example.uzradyab.data.remote.dto.PaymentResponseDto
}
