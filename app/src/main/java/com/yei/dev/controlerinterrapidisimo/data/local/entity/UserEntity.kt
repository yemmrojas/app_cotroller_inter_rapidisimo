package com.yei.dev.controlerinterrapidisimo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user session in the local database.
 *
 * @param id Primary key (always 1 since we only store one user session)
 * @param username The username of the authenticated user
 * @param identification The identification number of the user
 * @param name The full name of the user
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String,
    val identification: String,
    val name: String
)
