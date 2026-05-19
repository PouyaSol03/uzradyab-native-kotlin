package com.example.uzradyab.feature.auth.signin

data class SignInUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCheckingSession: Boolean = false,
    val errorMessage: String? = null,
    val signedIn: Boolean = false,
)
