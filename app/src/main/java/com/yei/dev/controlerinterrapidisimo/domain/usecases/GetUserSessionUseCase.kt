package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving the current user session.
 *
 * This use case retrieves the authenticated user session from local storage,
 * typically used during app startup to check if a user is already logged in
 * or when displaying user information.
 *
 * @param authRepository Repository for authentication operations
 */
class GetUserSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    /**
     * Executes the session retrieval operation.
     *
     * @return Flow emitting Result with UserSession if a session exists,
     *         null if no session exists, or an error if retrieval fails
     */
    operator fun invoke(): Flow<Result<UserSession?>> =
        authRepository.getUserSession()
}
