package com.example.uzradyab.data.repository

import com.example.uzradyab.data.remote.api.MapIrApi
import com.example.uzradyab.domain.repository.GeocoderRepository
import java.util.Collections
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocoderRepositoryImpl @Inject constructor(
    private val api: MapIrApi
) : GeocoderRepository {

    // ساختار کش شامل آدرس و زمان ثبت آن
    private data class CacheEntry(val address: String, val timestamp: Long)

    // سقف تعداد آیتم‌ها برای جلوگیری از پر شدن حافظه رم (OOM)
    private val MAX_CACHE_SIZE = 200

    // استفاده از LinkedHashMap با مکانیزم LRU و هماهنگ‌سازی (Thread-Safety)
    private val cache = Collections.synchronizedMap(
        object : LinkedHashMap<String, CacheEntry>(MAX_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
                // هرگاه تعداد آیتم‌ها از ۲۰۰ فراتر رفت، قدیمی‌ترین آیتم را حذف کن
                return size > MAX_CACHE_SIZE
            }
        }
    )

    private val CACHE_DURATION_MS = 3 * 60 * 1000L // 3 دقیقه
    private val API_KEY = com.example.uzradyab.BuildConfig.MAP_IR_API_KEY

    override suspend fun getAddress(lat: Double, lon: Double): String {
        val cacheKey = "$lat,$lon"
        val cachedData = cache[cacheKey]
        val currentTime = System.currentTimeMillis()

        // بررسی اعتبار کش (آیا کمتر از ۳ دقیقه گذشته است؟)
        if (cachedData != null && (currentTime - cachedData.timestamp) < CACHE_DURATION_MS) {
            return cachedData.address
        }

        return try {
            val response = api.getReverseGeocode(lat = lat, lon = lon)
            val jsonString = response.toString()

            com.example.uzradyab.core.debug.AppLogger.log(
                level = com.example.uzradyab.core.debug.LogLevel.INFO,
                tag = "GeocoderRepo",
                message = "New Geocode Response: $jsonString"
            )

            // اولویت با formatted_address است، در غیر این صورت address، در غیر این صورت پیام پیش‌فرض
            val newAddress = try {
                response.get("formatted_address")?.asString 
                    ?: response.get("address")?.asString 
                    ?: "در حال بررسی قالب آدرس..."
            } catch (e: Exception) {
                "در حال بررسی قالب آدرس..."
            }

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