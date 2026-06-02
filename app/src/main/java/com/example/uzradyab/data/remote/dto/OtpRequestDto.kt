package com.example.uzradyab.data.remote.dto

data class OtpRequestDto(
    val phone: String,
    val otp: String? = null,
)
