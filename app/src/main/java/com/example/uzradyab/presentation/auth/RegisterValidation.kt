package com.example.uzradyab.presentation.auth

data class PasswordRuleState(
    val hasMinimumLength: Boolean = false,
) {
    val isValid: Boolean
        get() = hasMinimumLength
}

fun passwordRuleState(password: String): PasswordRuleState {
    return PasswordRuleState(
        hasMinimumLength = password.length >= 8,
    )
}

fun isValidIranPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.length == 11 && phoneNumber.startsWith("09") && phoneNumber.all { it.isDigit() }
}

fun maskPhoneNumber(phoneNumber: String): String {
    return if (phoneNumber.length == 11) {
        "${phoneNumber.take(4)}xxxxx${phoneNumber.takeLast(2)}"
    } else {
        phoneNumber
    }
}
