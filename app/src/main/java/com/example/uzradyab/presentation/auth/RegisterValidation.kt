package com.example.uzradyab.presentation.auth

data class PasswordRuleState(
    val hasMinimumLength: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialCharacter: Boolean = false,
) {
    val isValid: Boolean
        get() = hasMinimumLength && hasDigit && hasSpecialCharacter
}

fun passwordRuleState(password: String): PasswordRuleState {
    return PasswordRuleState(
        hasMinimumLength = password.length >= 8,
        hasDigit = password.any { it.isDigit() },
        hasSpecialCharacter = password.any { !it.isLetterOrDigit() },
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
