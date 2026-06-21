package com.example.uzradyab.data.mapper

import com.example.uzradyab.data.remote.dto.SummaryReportDto
import com.example.uzradyab.domain.model.SummaryReport

fun SummaryReportDto.toDomain(): SummaryReport {
    return SummaryReport(
        deviceId = this.deviceId,
        deviceName = null, // نام دستگاه معمولا در DTO خلاصه گزارش نیست
        distance = this.distance,
        averageSpeed = this.averageSpeed,
        maxSpeed = this.maxSpeed,
        spentFuel = this.spentFuel,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        engineHours = this.engineHours,
        startTime = this.startTime,
        endTime = this.endTime,
        startAddress = this.startAddress,
        endAddress = this.endAddress
    )
}

fun com.example.uzradyab.data.remote.dto.StopReportDto.toDomain(): com.example.uzradyab.domain.model.StopReport {
    return com.example.uzradyab.domain.model.StopReport(
        deviceId = this.deviceId,
        deviceName = this.deviceName,
        positionId = this.positionId,
        latitude = this.latitude,
        longitude = this.longitude,
        startTime = this.startTime,
        endTime = this.endTime,
        address = this.address,
        duration = this.duration,
        engineHours = this.engineHours,
        spentFuel = this.spentFuel
    )
}

fun com.example.uzradyab.data.remote.dto.TripReportDto.toDomain(): com.example.uzradyab.domain.model.TripReport {
    return com.example.uzradyab.domain.model.TripReport(
        deviceId = this.deviceId,
        deviceName = this.deviceName,
        distance = this.distance,
        averageSpeed = this.averageSpeed,
        maxSpeed = this.maxSpeed,
        spentFuel = this.spentFuel,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        startTime = this.startTime,
        endTime = this.endTime,
        startPositionId = this.startPositionId,
        endPositionId = this.endPositionId,
        startLat = this.startLat,
        startLon = this.startLon,
        endLat = this.endLat,
        endLon = this.endLon,
        startAddress = this.startAddress,
        endAddress = this.endAddress,
        duration = this.duration,
        driverUniqueId = this.driverUniqueId,
        driverName = this.driverName
    )
}