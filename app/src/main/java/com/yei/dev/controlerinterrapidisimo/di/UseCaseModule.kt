package com.yei.dev.controlerinterrapidisimo.di

import com.yei.dev.controlerinterrapidisimo.BuildConfig
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.LocalitiesRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import com.yei.dev.controlerinterrapidisimo.domain.usecases.CheckVersionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetLocalitiesUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTableDataUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTablesUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetUserSessionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LoginUserUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LogoutUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.SyncDatabaseUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides use case instances.
 *
 * This module creates and provides all use case instances with their
 * required repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * Provides CheckVersionUseCase with the current app version.
     */
    @Provides
    @Singleton
    fun provideCheckVersionUseCase(
        versionRepository: VersionRepository
    ): CheckVersionUseCase = CheckVersionUseCase(
        versionRepository = versionRepository,
        appVersion = BuildConfig.VERSION_NAME
    )

    /**
     * Provides LoginUserUseCase.
     */
    @Provides
    @Singleton
    fun provideLoginUserUseCase(
        authRepository: AuthRepository
    ): LoginUserUseCase = LoginUserUseCase(authRepository)

    /**
     * Provides GetUserSessionUseCase.
     */
    @Provides
    @Singleton
    fun provideGetUserSessionUseCase(
        authRepository: AuthRepository
    ): GetUserSessionUseCase = GetUserSessionUseCase(authRepository)

    /**
     * Provides SyncDatabaseUseCase.
     */
    @Provides
    @Singleton
    fun provideSyncDatabaseUseCase(
        dataSyncRepository: DataSyncRepository
    ): SyncDatabaseUseCase = SyncDatabaseUseCase(dataSyncRepository)

    /**
     * Provides GetTablesUseCase.
     */
    @Provides
    @Singleton
    fun provideGetTablesUseCase(
        dataSyncRepository: DataSyncRepository
    ): GetTablesUseCase = GetTablesUseCase(dataSyncRepository)

    /**
     * Provides GetTableDataUseCase.
     */
    @Provides
    @Singleton
    fun provideGetTableDataUseCase(
        dataSyncRepository: DataSyncRepository
    ): GetTableDataUseCase = GetTableDataUseCase(dataSyncRepository)

    /**
     * Provides GetLocalitiesUseCase.
     */
    @Provides
    @Singleton
    fun provideGetLocalitiesUseCase(
        localitiesRepository: LocalitiesRepository
    ): GetLocalitiesUseCase = GetLocalitiesUseCase(localitiesRepository)

    /**
     * Provides LogoutUseCase.
     */
    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository
    ): LogoutUseCase = LogoutUseCase(authRepository)
}
