package com.example.uzradyab.map.tile

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import org.osmdroid.tileprovider.modules.SqlTileWriter
import java.util.concurrent.TimeUnit

/**
 * Singleton holder for heavy map dependencies to prevent resource leaks and database locks.
 * Ensures only a single OkHttpClient and SqlTileWriter (SQLite connection) exist across all map instances.
 */
object MapDependencies {
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // نگه داشتن ۱۰ کانکشن باز به مدت ۲ دقیقه برای سوییچ سریع استایل‌ها
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            // تایم‌اوت‌های کوتاه تا در صورت قطعی اینترنت، نقشه سریعاً فال‌بک کند و فریز نشود
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .build()
    }

}
