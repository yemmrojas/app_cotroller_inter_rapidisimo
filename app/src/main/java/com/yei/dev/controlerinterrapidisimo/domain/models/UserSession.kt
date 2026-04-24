package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents an authenticated user session.
 *
 * @param username The username of the authenticated user
 * @param identification The identification number of the user
 * @param name The full name of the user
 */
data class UserSession(
    val username: String,
    val identification: String,
    val name: String
)
