package com.example.uzradyab.di

import android.content.Context
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.core.debug.NetworkLogInterceptor
import com.example.uzradyab.core.network.PersistentCookieJar
import com.example.uzradyab.core.network.SessionEventBus
import com.example.uzradyab.core.network.UnauthorizedInterceptor
import com.example.uzradyab.data.remote.api.AuthHelperApi
import com.example.uzradyab.data.remote.api.MapIrApi
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.repository.GeocoderRepositoryImpl
import com.example.uzradyab.domain.repository.GeocoderRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val DEFAULT_SERVER_URL = "https://app.uzradyab.ir/"
private const val AUTH_HELPER_URL = "https://pay.uzradyab.ir/"
private const val NOTIFICATION_URL = "https://notification.uzradyab.ir/"
private const val AUTH_HELPER_RETROFIT = "authHelperRetrofit"
private const val NOTIFICATION_RETROFIT = "notificationRetrofit"

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
    fun provideOkHttpClient(
        cookieJar: PersistentCookieJar,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(unauthorizedInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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
        val notificationClient = client.newBuilder()
            .addInterceptor(com.example.uzradyab.core.network.CsrfInterceptor(cookieJar))
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
    fun provideNotificationApi(@Named(NOTIFICATION_RETROFIT) retrofit: Retrofit): com.example.uzradyab.data.remote.api.NotificationApi {
        return retrofit.create(com.example.uzradyab.data.remote.api.NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMapIrApi(client: OkHttpClient): MapIrApi {
        return Retrofit.Builder()
            .baseUrl("https://map.ir/")
            .client(client)
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
