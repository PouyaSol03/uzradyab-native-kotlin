package com.example.uzradyab.core.data

import com.example.uzradyab.core.model.AppUser
import com.example.uzradyab.core.model.Device
import com.example.uzradyab.core.model.Position
import com.example.uzradyab.core.network.TraccarApiClient

class SessionRepository(
    private val apiClient: TraccarApiClient,
) {
    suspend fun currentSession(): AppUser = apiClient.getSession()

    suspend fun signIn(phoneNumber: String, password: String): AppUser {
        return apiClient.signIn(phoneNumber = phoneNumber, password = password)
    }

    suspend fun signOut() {
        apiClient.signOut()
    }

    suspend fun devices(): List<Device> = apiClient.getDevices()

    suspend fun positions(): List<Position> = apiClient.getPositions()
}
