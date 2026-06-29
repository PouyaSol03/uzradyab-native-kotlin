package com.example.uzradyab.domain.usecase.auth

import com.example.uzradyab.domain.repository.RegistrationRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val registrationRepository: RegistrationRepository
) {
    suspend operator fun invoke(phone: String, otp: String): Result<Unit> {
        return registrationRepository.verifyOtp(phone, otp)
    }
}
