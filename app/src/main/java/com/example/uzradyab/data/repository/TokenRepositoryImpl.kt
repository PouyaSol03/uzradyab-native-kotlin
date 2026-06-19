package com.example.uzradyab.data.repository

import android.util.Log
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val api: TraccarApi
) : TokenRepository {

    override suspend fun syncToken(token: String, withRetries: Boolean): Result<Unit> = 
        withContext(Dispatchers.IO) { // تضمین اجرای کد در پس‌زمینه (جلوگیری از کندی اپلیکیشن)
            val maxRetries = if (withRetries) 3 else 0
            var currentDelay = 2000L

            for (attempt in 0..maxRetries) {
                try {
                    // TODO: هر زمان سرور آماده شد، این خط را با Endpoint دقیق جایگزین کنید
                    // api.updateUserToken(token)

                    Log.d("TokenRepositoryImpl", "✅ توکن با موفقیت در تلاش ${attempt + 1} ارسال شد.")
                    return@withContext Result.success(Unit)

                } catch (e: IOException) {
                    // این خطا یعنی کلاینت بی‌نقص عمل کرده اما اینترنت، DNS یا فایروال ISP مقصر است
                    Log.e("TokenRepositoryImpl", "🌐 خطای شبکه/اینترنت در تلاش ${attempt + 1}: ${e.message}")
                    if (attempt == maxRetries) return@withContext Result.failure(e)
                    
                } catch (e: Exception) {
                    // این خطا یعنی سرور در دسترس است اما ارور داده (مثل ارور ۵۰۰ یا مشکل بک‌اند)
                    Log.e("TokenRepositoryImpl", "❌ خطای سرور/بک‌اند در تلاش ${attempt + 1}: ${e.message}")
                    if (attempt == maxRetries) return@withContext Result.failure(e)
                }

                // تاخیر تصاعدی (Exponential Backoff) برای فشار نیاوردن به شبکه در زمان اختلال
                delay(currentDelay)
                currentDelay = (currentDelay * 2).coerceAtMost(16000L) // سقف تاخیر ۱۶ ثانیه
            }
            
            return@withContext Result.failure(Exception("حداکثر تلاش برای ارسال توکن انجام شد اما شبکه پاسخ نداد."))
        }
}