package com.example.uzradyab.domain.usecase

import com.example.uzradyab.domain.model.Device
import com.example.uzradyab.domain.model.Position
import com.example.uzradyab.domain.repository.DeviceRepository
import com.example.uzradyab.domain.repository.PositionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class HomeSnapshot(
    val devices: List<Device>,
    val latestPositions: Map<Long, Position>,
)

class ObserveHomeSnapshotUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val positionRepository: PositionRepository,
) {
    operator fun invoke(): Flow<HomeSnapshot> {
        return combine(
            deviceRepository.observeDevices(),
            positionRepository.observeLatestPositions(),
        ) { devices, latestPositions ->
            HomeSnapshot(devices = devices, latestPositions = latestPositions)
        }
    }
}
