package com.yei.dev.controlerinterrapidisimo.domain.repositories

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication and session management operations.
 *
 * This repository handles user authentication with the remote Authentication Service,
 * manages user session persistence in the local database, and provides session
 * retrieval and cleanup operations.
 *
 * Business Purpose:
 * - Authenticates users with the remote service using credentials
 * - Persists authenticated user sessions locally for offline access
 * - Enables session restoration across app restarts
 * - Manages user logout and session cleanup
 */
interface AuthRepository {
    /**
     * Authenticates a user and saves the session.
     *
     * This method handles the complete login flow: authenticates with the remote service,
     * converts the response to a UserSession, and persists it locally.
     *
     * @param username The username for authentication
     * @param password The user's password
     * @param mac The device MAC address for device identification
     * @return Flow emitting Result with UserSession on success,
     *         or an error if authentication or session save fails
     */
    fun login(
        username: String,
        password: String,
        mac: String
    ): Flow<Result<UserSession>>

    /**
     * Retrieves the current user session from the local database.
     *
     * @return Flow emitting Result with UserSession if a session exists,
     *         null if no session exists, or an error if retrieval fails
     */
    fun getUserSession(): Flow<Result<UserSession?>>

    /**
     * Clears the current user session from the local database.
     *
     * This is typically called during logout operations.
     *
     * @return Flow emitting Result with Unit on success,
     *         or an error if the clear operation fails
     */
    fun clearUserSession(): Flow<Result<Unit>>
}
