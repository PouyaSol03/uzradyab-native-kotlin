package com.example.uzradyab.domain.usecase.device

import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<List<Device>> {
        return deviceRepository.observeDevices()
    }
}
