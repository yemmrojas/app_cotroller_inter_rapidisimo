package com.yei.dev.controlerinterrapidisimo.di

import com.yei.dev.controlerinterrapidisimo.data.local.AppDatabase
import com.yei.dev.controlerinterrapidisimo.data.local.dao.DynamicTableDao
import com.yei.dev.controlerinterrapidisimo.data.local.dao.UserDao
import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import com.yei.dev.controlerinterrapidisimo.data.mappers.AuthResponseConverter
import com.yei.dev.controlerinterrapidisimo.data.mappers.ColumnDefinitionConverter
import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.mappers.LocalityConverter
import com.yei.dev.controlerinterrapidisimo.data.mappers.TableSchemaConverter
import com.yei.dev.controlerinterrapidisimo.data.mappers.UserEntityToDomainConverter
import com.yei.dev.controlerinterrapidisimo.data.mappers.UserSessionToEntityConverter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.AuthApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.DataSyncApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.LocalitiesApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.api.VersionApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalityDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import com.yei.dev.controlerinterrapidisimo.data.repositories.AuthRepositoryImpl
import com.yei.dev.controlerinterrapidisimo.data.repositories.DataSyncRepositoryImpl
import com.yei.dev.controlerinterrapidisimo.data.repositories.LocalitiesRepositoryImpl
import com.yei.dev.controlerinterrapidisimo.data.repositories.VersionRepositoryImpl
import com.yei.dev.controlerinterrapidisimo.domain.models.AuthResponse
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.LocalitiesRepository
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations and data mappers.
 *
 * This module binds repository interfaces to their implementations and provides
 * converter instances for data transformation between layers.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds VersionRepository interface to its implementation.
     */
    @Binds
    @Singleton
    abstract fun bindVersionRepository(
        impl: VersionRepositoryImpl
    ): VersionRepository

    /**
     * Binds AuthRepository interface to its implementation.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Binds DataSyncRepository interface to its implementation.
     */
    @Binds
    @Singleton
    abstract fun bindDataSyncRepository(
        impl: DataSyncRepositoryImpl
    ): DataSyncRepository

    /**
     * Binds LocalitiesRepository interface to its implementation.
     */
    @Binds
    @Singleton
    abstract fun bindLocalitiesRepository(
        impl: LocalitiesRepositoryImpl
    ): LocalitiesRepository

    companion object {
        /**
         * Provides AuthResponseConverter for DTO to domain model conversion.
         */
        @Provides
        @Singleton
        fun provideAuthResponseConverter(): Converter<AuthResponseDto, AuthResponse> =
            AuthResponseConverter()

        /**
         * Provides UserEntityToDomainConverter for entity to domain model conversion.
         */
        @Provides
        @Singleton
        fun provideUserEntityToDomainConverter(): Converter<UserEntity, UserSession> =
            UserEntityToDomainConverter()

        /**
         * Provides UserSessionToEntityConverter for domain model to entity conversion.
         */
        @Provides
        @Singleton
        fun provideUserSessionToEntityConverter(): Converter<UserSession, UserEntity> =
            UserSessionToEntityConverter()

        /**
         * Provides LocalityConverter for DTO to domain model conversion.
         */
        @Provides
        @Singleton
        fun provideLocalityConverter(): Converter<LocalityDto, Locality> =
            LocalityConverter()

        /**
         * Provides ColumnDefinitionConverter for DTO to domain model conversion.
         */
        @Provides
        @Singleton
        fun provideColumnDefinitionConverter(): Converter<ColumnDefinitionDto, ColumnDefinition> =
            ColumnDefinitionConverter()

        /**
         * Provides TableSchemaConverter for DTO to domain model conversion.
         */
        @Provides
        @Singleton
        fun provideTableSchemaConverter(
            columnDefinitionConverter: Converter<ColumnDefinitionDto, ColumnDefinition>
        ): Converter<TableSchemaDto, TableSchema> =
            TableSchemaConverter(columnDefinitionConverter)
    }
}
