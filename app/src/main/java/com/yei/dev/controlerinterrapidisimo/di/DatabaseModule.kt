package com.yei.dev.controlerinterrapidisimo.di

import android.content.Context
import androidx.room.Room
import com.yei.dev.controlerinterrapidisimo.data.local.AppDatabase
import com.yei.dev.controlerinterrapidisimo.data.local.dao.DynamicTableDao
import com.yei.dev.controlerinterrapidisimo.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 *
 * This module configures and provides:
 * - Room database instance
 * - All DAO instances (UserDao, DynamicTableDao)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "controller_app_database"

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DATABASE_NAME
    )
        .fallbackToDestructiveMigration(false)
        .build()

    /**
     * Provides UserDao from the database.
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao =
        database.userDao()

    /**
     * Provides DynamicTableDao with database access.
     *
     * DynamicTableDao requires direct database access for raw SQL operations.
     */
    @Provides
    @Singleton
    fun provideDynamicTableDao(database: AppDatabase): DynamicTableDao =
        DynamicTableDao(database.openHelper.writableDatabase)
}
