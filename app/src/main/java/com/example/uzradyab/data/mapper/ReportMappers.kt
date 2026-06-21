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
        endOdometer = this.endOdometer
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