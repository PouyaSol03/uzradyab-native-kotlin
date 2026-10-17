package com.example.uzradyab.presentation.events

import androidx.compose.runtime.Immutable

@Immutable
data class EventUiModel(
    val id: Long,
    val title: String,
    val time: String,
    /** True for `commandResult` events, which show the device reply under the title. */
    val isCommandResult: Boolean = false,
    /** Device reply text; null when the device sent no text. */
    val commandResult: String? = null,
)
