package com.example.uzradyab.domain.model

data class UserSession(
    val id: Long,
    val name: String,
    val email: String,
    val readonly: Boolean,
)
