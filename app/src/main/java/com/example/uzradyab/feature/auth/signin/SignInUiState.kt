package com.example.uzradyab.feature.auth.signin

data class SignInUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
)
