package com.example.uzradyab.core.network

import com.example.uzradyab.core.model.AppUser
import com.example.uzradyab.core.model.Device
import com.example.uzradyab.core.model.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class TraccarApiClient(
    private val client: OkHttpClient,
    private val cookieJar: PersistentCookieJar,
) {
    suspend fun getSession(): AppUser = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/api/session")
            .get()
            .build()

        client.executeJsonObject(request).toUser()
    }

    suspend fun signIn(phoneNumber: String, password: String): AppUser = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("email", phoneNumber)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/api/session")
            .post(body)
            .header("Accept", "application/json")
            .build()

        client.executeJsonObject(request).toUser()
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/api/session")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            cookieJar.clear()
            if (!response.isSuccessful && response.code != 401) {
                throw ApiException(response.code, response.body?.string().orEmpty().ifBlank { "Sign out failed" })
            }
        }
    }

    suspend fun getDevices(): List<Device> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/api/devices")
            .get()
            .header("Accept", "application/json")
            .build()

        client.executeJsonArray(request).toDevices()
    }

    suspend fun getPositions(): List<Position> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/api/positions")
            .get()
            .header("Accept", "application/json")
            .build()

        client.executeJsonArray(request).toPositions()
    }

    private fun OkHttpClient.executeJsonObject(request: Request): JSONObject {
        return newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw ApiException(response.code, body.ifBlank { "HTTP ${response.code}" })
            JSONObject(body)
        }
    }

    private fun OkHttpClient.executeJsonArray(request: Request): JSONArray {
        return newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw ApiException(response.code, body.ifBlank { "HTTP ${response.code}" })
            JSONArray(body)
        }
    }

    private fun JSONObject.toUser(): AppUser = AppUser(
        id = optLong("id"),
        name = optString("name").ifBlank { optString("email") },
        email = optString("email"),
    )

    private fun JSONArray.toDevices(): List<Device> = List(length()) { index ->
        getJSONObject(index).run {
            Device(
                id = optLong("id"),
                name = optString("name").ifBlank { optString("uniqueId") },
                uniqueId = optString("uniqueId"),
                status = optString("status", "unknown"),
                lastUpdate = nullableString("lastUpdate"),
                expirationTime = nullableString("expirationTime"),
            )
        }
    }

    private fun JSONArray.toPositions(): List<Position> = List(length()) { index ->
        getJSONObject(index).run {
            Position(
                deviceId = optLong("deviceId"),
                latitude = optDouble("latitude"),
                longitude = optDouble("longitude"),
                speed = optDouble("speed"),
                serverTime = nullableString("serverTime"),
            )
        }
    }

    private fun JSONObject.nullableString(name: String): String? {
        return if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }
    }
}
