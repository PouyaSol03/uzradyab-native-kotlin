package com.example.uzradyab.presentation.events

import androidx.compose.runtime.Immutable

@Immutable
data class EventUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val time: String,
)
