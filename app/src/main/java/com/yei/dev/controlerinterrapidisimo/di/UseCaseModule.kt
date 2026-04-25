package com.yei.dev.controlerinterrapidisimo.di

import com.yei.dev.controlerinterrapidisimo.BuildConfig
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import com.yei.dev.controlerinterrapidisimo.domain.usecases.CheckVersionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides use case instances that require external values.
 *
 * Only use cases that cannot be satisfied by constructor injection are provided here.
 * Other use cases with @Inject constructors are automatically provided by Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    /**
     * Provides CheckVersionUseCase with the current app version.
     * 
     * This use case requires BuildConfig.VERSION_NAME which cannot be injected
     * automatically, so it needs an explicit @Provides method.
     */
    @Provides
    @Singleton
    fun provideCheckVersionUseCase(
        versionRepository: VersionRepository
    ): CheckVersionUseCase = CheckVersionUseCase(
        versionRepository = versionRepository,
        appVersion = BuildConfig.VERSION_NAME
    )
}
