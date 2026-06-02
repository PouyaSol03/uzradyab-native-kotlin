package com.example.uzradyab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val singletonId: Int = 1,
    val id: Long,
    val name: String,
    val email: String,
    val readonly: Boolean,
)
