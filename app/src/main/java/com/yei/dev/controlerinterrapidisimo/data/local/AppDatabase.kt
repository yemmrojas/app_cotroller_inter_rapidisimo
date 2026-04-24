package com.yei.dev.controlerinterrapidisimo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yei.dev.controlerinterrapidisimo.data.local.dao.UserDao
import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity

/**
 * Room database for the Controller APP.
 *
 * This database manages both static entities (UserEntity) and dynamic tables
 * created at runtime based on schema synchronization from the remote service.
 *
 * @property userDao Data access object for user session operations
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Provides access to user session operations.
     */
    abstract fun userDao(): UserDao
}
