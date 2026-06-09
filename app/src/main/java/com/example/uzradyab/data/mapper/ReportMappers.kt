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