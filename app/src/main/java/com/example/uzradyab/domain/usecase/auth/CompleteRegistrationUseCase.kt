package com.example.uzradyab.domain.usecase.auth

import com.example.uzradyab.domain.manager.FcmTokenManager
import com.example.uzradyab.domain.repository.RegistrationRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class CompleteRegistrationUseCase @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository
) {
    suspend operator fun invoke(name: String, phone: String, pass: String): Result<Unit> {
        val result = registrationRepository.createUserAndLogin(name, phone, pass)
        if (result.isSuccess) {
            // Background sync post-registration
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                fcmTokenManager.syncCurrentToken()
                deviceRepository.refreshDevices()
                positionRepository.refreshLatestPositions()
            }
        }
        return result.map { }
    }
}
