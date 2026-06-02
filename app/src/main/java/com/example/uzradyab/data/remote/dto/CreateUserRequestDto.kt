package com.example.uzradyab.data.remote.dto

data class CreateUserRequestDto(
    val login: String,
    val email: String,
    val password: String,
    val name: String,
    val phone: String = "",
)
