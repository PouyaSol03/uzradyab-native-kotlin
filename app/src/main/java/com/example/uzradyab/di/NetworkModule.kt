package com.example.uzradyab.di

import android.content.Context
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.core.debug.MockTraccarInterceptor
import com.example.uzradyab.core.debug.NetworkLogInterceptor
import com.example.uzradyab.core.network.CsrfInterceptor
import com.example.uzradyab.core.network.NetworkErrorInterceptor
import com.example.uzradyab.core.network.NetworkEventBus
import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.core.network.SessionEventBus
import com.example.uzradyab.core.network.UnauthorizedInterceptor
import com.example.uzradyab.data.remote.api.AuthHelperApi
import com.example.uzradyab.data.remote.api.MapIrApi
import com.example.uzradyab.data.remote.api.NotificationApi
import com.example.uzradyab.data.remote.api.AppConfigApi
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.repository.GeocoderRepositoryImpl
import com.example.uzradyab.domain.repository.GeocoderRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

private val DEFAULT_SERVER_URL = BuildConfig.API_BASE_URL
private val AUTH_HELPER_URL = BuildConfig.PAY_BASE_URL
private val NOTIFICATION_URL = BuildConfig.NOTIFICATION_BASE_URL

private const val AUTH_HELPER_RETROFIT = "authHelperRetrofit"
private const val NOTIFICATION_RETROFIT = "notificationRetrofit"
private const val BARE_CLIENT = "bareClient"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieJar(@ApplicationContext context: Context): PersistentCookieJar {
        return PersistentCookieJar(context)
    }

    @Provides
    @Singleton
    fun provideUnauthorizedInterceptor(sessionEventBus: SessionEventBus): UnauthorizedInterceptor {
        return UnauthorizedInterceptor(sessionEventBus)
    }

    @Provides
    @Singleton
    fun provideNetworkErrorInterceptor(networkEventBus: NetworkEventBus): NetworkErrorInterceptor {
        return NetworkErrorInterceptor(networkEventBus)
    }

    /**
     * Primary OkHttpClient for Traccar API
     * Includes Session Cookies, Unauthorized Interceptor, and heavy connection pooling.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: PersistentCookieJar,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        networkErrorInterceptor: NetworkErrorInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(networkErrorInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .connectionPool(ConnectionPool(8, 3, TimeUnit.MINUTES)) // Component 2 Optimization
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(NetworkLogInterceptor())
            builder.addInterceptor(MockTraccarInterceptor())
        }
        return builder.build()
    }

    /**
     * Bare OkHttpClient without cookies, auth interceptors, or mock interceptors.
     * Used for third-party APIs (e.g. MapIr) that don't need Traccar session state.
     */
    @Provides
    @Singleton
    @Named(BARE_CLIENT)
    fun provideBareOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(4, 2, TimeUnit.MINUTES)) // Lightweight pool for third-party
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(NetworkLogInterceptor())
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_SERVER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named(AUTH_HELPER_RETROFIT)
    fun provideAuthHelperRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AUTH_HELPER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named(NOTIFICATION_RETROFIT)
    fun provideNotificationRetrofit(
        client: OkHttpClient,
        gson: Gson,
        cookieJar: PersistentCookieJar
    ): Retrofit {
        // Reuses the primary OkHttp pool but adds the specific CSRF interceptor
        val notificationClient = client.newBuilder()
            .addInterceptor(CsrfInterceptor(cookieJar))
            .build()
            
        return Retrofit.Builder()
            .baseUrl(NOTIFICATION_URL)
            .client(notificationClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTraccarApi(retrofit: Retrofit): TraccarApi {
        return retrofit.create(TraccarApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthHelperApi(@Named(AUTH_HELPER_RETROFIT) retrofit: Retrofit): AuthHelperApi {
        return retrofit.create(AuthHelperApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(@Named(NOTIFICATION_RETROFIT) retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppConfigApi(@Named(AUTH_HELPER_RETROFIT) retrofit: Retrofit): AppConfigApi {
        return retrofit.create(AppConfigApi::class.java)
    }

    /**
     * Map.ir API correctly utilizes the Bare Client to prevent leaking Traccar cookies
     */
    @Provides
    @Singleton
    fun provideMapIrApi(@Named(BARE_CLIENT) bareClient: OkHttpClient): MapIrApi {
        return Retrofit.Builder()
            .baseUrl("https://rg.exirfirm.com/")
            .client(bareClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MapIrApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocoderRepository(api: MapIrApi): GeocoderRepository {
        return GeocoderRepositoryImpl(api)
    }
}