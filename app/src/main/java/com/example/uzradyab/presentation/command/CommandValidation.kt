package com.example.uzradyab.presentation.command

import com.example.uzradyab.core.utils.FormatUtils.toPersianDigits

/** Pure validation for command values. Persian and Arabic digits are accepted and treated as Latin. */
object CommandValidation {

    private val PHONE_REGEX = Regex("^\\+?[0-9]{5,15}$")
    private val NUMBER_REGEX = Regex("^[0-9]+$")

    const val PHONE_ERROR = "شماره معتبر نیست."

    fun normalizeDigits(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    in '۰'..'۹' -> '0' + (char - '۰')
                    in '٠'..'٩' -> '0' + (char - '٠')
                    else -> char
                }
            )
        }
    }

    fun isValid(param: CommandParam, raw: String): Boolean {
        val value = normalizeDigits(raw).trim()
        return when (param) {
            is NumberParam -> NUMBER_REGEX.matches(value) && value.toIntOrNull()?.let { it in param.min..param.max } == true
            is NotifyParam -> param.options.any { it.value == value }
            is PhoneParam -> if (value.isEmpty()) !param.required else PHONE_REGEX.matches(value)
        }
    }

    /** Error text for a field, only once the user has typed something. Empty fields never show an error. */
    fun errorFor(param: CommandParam, raw: String): String? {
        if (raw.isBlank() || isValid(param, raw)) return null
        return when (param) {
            is NumberParam -> numberError(param)
            is PhoneParam -> PHONE_ERROR
            is NotifyParam -> null
        }
    }

    fun numberError(param: NumberParam): String =
        "عددی بین ${param.min} تا ${param.max} وارد کنید.".toPersianDigits()

    fun allValid(params: List<CommandParam>, values: Map<String, String>): Boolean =
        params.all { isValid(it, values[it.key].orEmpty()) }

    /**
     * Step-by-step unlocking: a field is locked until every field above it is valid, and a phone
     * also waits for the previous phone to be filled. The first unlocked empty/invalid field is
     * highlighted, except an empty optional phone.
     */
    fun evaluateFields(params: List<CommandParam>, values: Map<String, String>): List<FieldUiState> {
        var previousValid = true
        var previousPhoneFilled = true
        var highlightTaken = false
        return params.map { param ->
            val value = values[param.key].orEmpty()
            val valid = isValid(param, value)
            val locked = !previousValid || (param is PhoneParam && !previousPhoneFilled)
            val optionalEmptyPhone = param is PhoneParam && !param.required && value.isBlank()
            val highlighted = !highlightTaken && !locked && (value.isBlank() || !valid) && !optionalEmptyPhone
            if (highlighted) highlightTaken = true
            previousValid = previousValid && valid
            if (param is PhoneParam) previousPhoneFilled = value.isNotBlank()
            FieldUiState(
                param = param,
                value = value,
                locked = locked,
                highlighted = highlighted,
                error = if (locked) null else errorFor(param, value),
            )
        }
    }
}

data class FieldUiState(
    val param: CommandParam,
    val value: String,
    val locked: Boolean,
    val highlighted: Boolean,
    val error: String?,
)
