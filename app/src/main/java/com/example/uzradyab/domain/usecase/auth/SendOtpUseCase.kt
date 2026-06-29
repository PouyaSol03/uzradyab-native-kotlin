package com.example.uzradyab.domain.usecase.auth

import com.example.uzradyab.domain.repository.RegistrationRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val registrationRepository: RegistrationRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return registrationRepository.sendOtp(phone)
    }
}
