package com.yei.dev.controlerinterrapidisimo.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yei.dev.controlerinterrapidisimo.data.remote.JsonParsingInterceptor
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.AuthApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.DataSyncApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.LocalitiesApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.VersionApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module that provides network-related dependencies.
 *
 * This module configures and provides:
 * - Retrofit instance with Kotlinx Serialization
 * - OkHttp client with interceptors
 * - All API service interfaces
 * - NetworkHandler for safe API calls
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // OFFICIAL API ENDPOINTS FROM DOCUMENTATION:
    // Based on the official document, we have different base URLs for different services:
    // - Version Control & Data Sync & Localities: https://apitesting.interrapidisimo.co/apicontrollerpruebas/
    // - Authentication: https://apitesting.interrapidisimo.co/FtEntregaElectronica/MultiCanales/ApiSeguridadPruebas/
    //
    // For now using the main API base URL. Auth service will need special handling.
    // TODO: Consider using different Retrofit instances for different base URLs
    private const val BASE_URL = "https://apitesting.interrapidisimo.co/"
    private const val TIMEOUT_SECONDS = 30L

    /**
     * Provides a configured Json instance for Kotlinx Serialization.
     *
     * Configured with lenient mode to handle escaped characters from the
     * Interrapidisimo API (e.g., \n in Password field).
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Provides JsonParsingInterceptor for handling JSON edge cases.
     */
    @Provides
    @Singleton
    fun provideJsonParsingInterceptor(): JsonParsingInterceptor =
        JsonParsingInterceptor()

    /**
     * Provides HttpLoggingInterceptor for debugging API requests/responses.
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    /**
     * Provides configured OkHttpClient with interceptors and timeouts.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        jsonParsingInterceptor: JsonParsingInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(jsonParsingInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /**
     * Provides configured Retrofit instance for main API services.
     * Includes both Scalars converter (for simple string responses) and JSON converter.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create()) // For simple string responses
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())) // For JSON responses
        .build()

    /**
     * Provides configured Retrofit instance for authentication service.
     * Uses different base URL as specified in the official documentation.
     */
    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create()) // For simple string responses
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())) // For JSON responses
        .build()

    /**
     * Provides VersionApiService instance.
     */
    @Provides
    @Singleton
    fun provideVersionApiService(retrofit: Retrofit): VersionApiService =
        retrofit.create(VersionApiService::class.java)

    /**
     * Provides AuthApiService instance using the authentication Retrofit instance.
     */
    @Provides
    @Singleton
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    /**
     * Provides DataSyncApiService instance.
     */
    @Provides
    @Singleton
    fun provideDataSyncApiService(retrofit: Retrofit): DataSyncApiService =
        retrofit.create(DataSyncApiService::class.java)

    /**
     * Provides LocalitiesApiService instance.
     */
    @Provides
    @Singleton
    fun provideLocalitiesApiService(retrofit: Retrofit): LocalitiesApiService =
        retrofit.create(LocalitiesApiService::class.java)

    /**
     * Provides NetworkHandler for safe API call execution.
     */
    @Provides
    @Singleton
    fun provideNetworkHandler(
        @ApplicationContext context: Context,
    ): NetworkHandler = NetworkHandler(context)
}
