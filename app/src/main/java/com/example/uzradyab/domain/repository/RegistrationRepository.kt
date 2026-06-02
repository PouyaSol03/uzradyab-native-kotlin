package com.example.uzradyab.domain.repository

import com.example.uzradyab.domain.model.UserSession

interface RegistrationRepository {
    suspend fun sendOtp(phoneNumber: String): Result<Unit>
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<Unit>
    suspend fun createUserAndLogin(
        name: String,
        phoneNumber: String,
        password: String,
    ): Result<UserSession>
}
