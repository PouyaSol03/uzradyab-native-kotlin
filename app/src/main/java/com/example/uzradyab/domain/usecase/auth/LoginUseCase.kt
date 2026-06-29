package com.example.uzradyab.domain.usecase.auth

import com.example.uzradyab.domain.manager.FcmTokenManager
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val fcmTokenManager: FcmTokenManager,
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository
) {
    suspend operator fun invoke(phone: String, pass: String): Result<Unit> {
        val result = authRepository.login(phone, pass)
        if (result.isSuccess) {
            // Background sync post-login
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                fcmTokenManager.syncCurrentToken()
                deviceRepository.refreshDevices()
                positionRepository.refreshLatestPositions()
            }
        }
        return result.map { }
    }
}
