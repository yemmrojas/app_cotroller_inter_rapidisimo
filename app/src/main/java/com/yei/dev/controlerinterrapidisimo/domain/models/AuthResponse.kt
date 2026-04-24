package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the response from the authentication service.
 *
 * @param username The username of the authenticated user
 * @param identification The identification number of the user (not persisted for security)
 * @param name The full name of the user
 * @param token Optional authentication token (if provided by the API)
 */
data class AuthResponse(
    val username: String,
    val identification: String,
    val name: String,
    val token: String? = null
) {
    /**
     * Converts this AuthResponse to a UserSession.
     * Note: identification is intentionally not included for security reasons.
     */
    fun toUserSession(): UserSession = UserSession(
        username = username,
        name = name
    )
}
