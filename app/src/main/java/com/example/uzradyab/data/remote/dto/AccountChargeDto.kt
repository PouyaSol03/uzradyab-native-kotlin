package com.example.uzradyab.data.remote.dto

data class AccountChargeDto(
    val id: Int,
    val period: String,
    val amount: String,
    val description: String? = null
)
