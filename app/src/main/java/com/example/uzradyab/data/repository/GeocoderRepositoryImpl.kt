package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.MapIrApi
import com.example.uzradyab.domain.repository.GeocoderRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocoderRepositoryImpl @Inject constructor(
    private val api: MapIrApi
) : GeocoderRepository {

    // ساختار کش شامل آدرس و زمان ثبت آن
    private data class CacheEntry(val address: String, val timestamp: Long)

    // استفاده از ConcurrentHashMap برای Thread-Safety
    private val cache = ConcurrentHashMap<String, CacheEntry>()

    private val CACHE_DURATION_MS = 3 * 60 * 1000L // 3 دقیقه
    private val API_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImNjYTg5MGViMzJlNzA4N2Q0ZDI3MjI5ZDBjMmZkYjFkOTRlNWQyOTUyNDc3NzhjN2M1Y2YxYmFkNzhiMjFkMGQ5NDNkMzg2ZTc3MDBhNGE1In0.eyJhdWQiOiI0MDAzMyIsImp0aSI6ImNjYTg5MGViMzJlNzA4N2Q0ZDI3MjI5ZDBjMmZkYjFkOTRlNWQyOTUyNDc3NzhjN2M1Y2YxYmFkNzhiMjFkMGQ5NDNkMzg2ZTc3MDBhNGE1IiwiaWF0IjoxNzc3Nzk3Nzg2LCJuYmYiOjE3Nzc3OTc3ODYsImV4cCI6MTc4MDM4OTc4Niwic3ViIjoiIiwic2NvcGVzIjpbImJhc2ljIl19.WsvBtor5Xp1MPC1hF2I8kea6iAzOCyc_skxeTmSNDzUeLdlMe5nhqCMdG7lGbIKEQTGKnZMUVPBoiZ0rsLtDBwmTMAUVtrvkucqBBccQBXIFH5vZslpVVbwyDHjSm9farffrORQX7Rn-MnhSOPAfUqap2gSYPyehtQFSm8Lqb3Zlst1pr6_z_0ki41Ln-wMaWChHA66w38mVYCB0o8kzDBb5zvl1ZQKBvQjLH7CWNeT4l5BlsnYOM8Rn96xX-yjT6bfC77jl0-s5mxtkRoJHiR26hOFM3t_ZhY9cFQpPINc7oWbKe-l0a-rPg2ipBjukqJpdouJVjuVunDP0amCuhg" // TODO: کلید API خود را اینجا قرار دهید

    override suspend fun getAddress(lat: Double, lon: Double): String {
        val cacheKey = "$lat,$lon"
        val cachedData = cache[cacheKey]
        val currentTime = System.currentTimeMillis()

        // بررسی اعتبار کش (آیا کمتر از ۳ دقیقه گذشته است؟)
        if (cachedData != null && (currentTime - cachedData.timestamp) < CACHE_DURATION_MS) {
            return cachedData.address
        }

        return try {
            val response = api.getReverseGeocode(apiKey = API_KEY, lat = lat, lon = lon)

            // اولویت با formatted_address است، در غیر این صورت address، در غیر این صورت پیام پیش‌فرض
            val newAddress = response.formattedAddress ?: response.address ?: "آدرس نامشخص"

            // ذخیره در کش
            cache[cacheKey] = CacheEntry(address = newAddress, timestamp = currentTime)

            newAddress
        } catch (e: Exception) {
            android.util.Log.e("GeocoderRepo", "خطا در دریافت آدرس", e)
            // اگر قبلاً دیتایی در کش بود (حتی منقضی شده)، در صورت قطعی اینترنت همان را نشان بده
            cachedData?.address ?: "آدرس در دسترس نیست"
        }
    }
}