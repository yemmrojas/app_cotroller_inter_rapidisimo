package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for authenticating a user and creating a session.
 *
 * This use case delegates to the AuthRepository which handles the complete
 * login flow including authentication and session persistence.
 *
 * @param authRepository Repository for authentication operations
 */
class LoginUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    /**
     * Executes the login operation.
     *
     * @param username The username for authentication
     * @param password The user's password
     * @param mac The device MAC address
     * @return Flow emitting Result with UserSession on successful authentication,
     *         or an error if authentication or session save fails
     */
    operator fun invoke(
        username: String,
        password: String,
        mac: String,
    ): Flow<Result<UserSession>> =
        authRepository.login(username, password, mac)
}
