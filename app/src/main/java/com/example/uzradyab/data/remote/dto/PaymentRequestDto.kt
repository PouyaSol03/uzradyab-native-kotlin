package com.example.uzradyab.data.remote.dto

data class PaymentRequestDto(
    val amount: Long,
    val period: String,
    val uniqueId: String,
    val phone: String,
    val name: String,
    val id: Long,
    val accountChargeId: Int
)
