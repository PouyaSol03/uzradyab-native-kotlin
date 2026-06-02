package com.example.uzradyab.data.remote.dto

data class SessionDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val readonly: Boolean = false,
)
