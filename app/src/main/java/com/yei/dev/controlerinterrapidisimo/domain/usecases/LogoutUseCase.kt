package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for logging out a user and clearing their session.
 *
 * This use case handles the logout operation by clearing the user session
 * from local storage, effectively ending the authenticated session.
 *
 * @param authRepository Repository for authentication operations
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    /**
     * Executes the logout operation.
     *
     * @return Flow emitting Result with Unit on successful logout,
     *         or an error if the session clear operation fails
     */
    operator fun invoke(): Flow<Result<Unit>> =
        authRepository.clearUserSession()

}
