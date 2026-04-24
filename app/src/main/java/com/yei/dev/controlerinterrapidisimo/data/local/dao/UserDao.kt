package com.yei.dev.controlerinterrapidisimo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity

/**
 * Data Access Object for user session operations.
 *
 * Provides methods to insert, query, and delete user session data
 * from the local database.
 */
@Dao
interface UserDao {
    /**
     * Inserts or replaces a user session in the database.
     *
     * @param user The user entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Retrieves the current user session from the database.
     *
     * @return UserEntity if a session exists, null otherwise
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserEntity?

    /**
     * Deletes all user sessions from the database.
     */
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}
