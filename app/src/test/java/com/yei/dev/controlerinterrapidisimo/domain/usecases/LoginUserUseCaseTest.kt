package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for LoginUserUseCase.
 *
 * Tests verify that the use case correctly delegates to the AuthRepository
 * and returns the expected results for various scenarios.
 */
class LoginUserUseCaseTest {

    @Test
    fun `invoke should return success with UserSession when login succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesAuthRepository(
            result = Result.Success(scenario.expectedUserSession),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut(
            username = scenario.username,
            password = scenario.password,
            mac = scenario.mac,
        ).collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedUserSession)
        }
    }

    @Test
    fun `invoke should return error when login fails`() = runTest {
        // Given
        val scenario = providesErrorScenarios().first()
        val mockRepository = providesAuthRepository(
            result = Result.Error(scenario.error),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut(
            username = scenario.username,
            password = scenario.password,
            mac = scenario.mac,
        ).collect { result ->
            // Then
            assert(result is Result.Error)
            assertEquals((result as Result.Error).error, scenario.error)
        }
    }

    // Provider methods
    companion object {
        fun providesSuccessScenarios() = listOf(
            SuccessScenario(
                description = "Valid credentials login",
                username = "testuser",
                password = "password123",
                mac = "00:11:22:33:44:55",
                expectedUserSession = UserSession(
                    username = "testuser",
                    name = "Test User",
                ),
            ),
            SuccessScenario(
                description = "Login with different user",
                username = "admin",
                password = "admin123",
                mac = "AA:BB:CC:DD:EE:FF",
                expectedUserSession = UserSession(
                    username = "admin",
                    name = "Admin User",
                ),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Network error during login",
                username = "testuser",
                password = "password123",
                mac = "00:11:22:33:44:55",
                error = AppError.NetworkError("No internet connection"),
            ),
            ErrorScenario(
                description = "Invalid credentials",
                username = "wronguser",
                password = "wrongpass",
                mac = "00:11:22:33:44:55",
                error = AppError.ApiError(401, "Unauthorized"),
            ),
            ErrorScenario(
                description = "Database error saving session",
                username = "testuser",
                password = "password123",
                mac = "00:11:22:33:44:55",
                error = AppError.DatabaseError("Failed to save session"),
            ),
        )

        private fun providesSut(
            authRepository: AuthRepository,
        ): LoginUserUseCase =
            LoginUserUseCase(authRepository = authRepository)

        private fun providesAuthRepository(
            result: Result<UserSession>,
        ): AuthRepository {
            return mockk<AuthRepository> {
                every { login(any(), any(), any()) } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val username: String,
        val password: String,
        val mac: String,
        val expectedUserSession: UserSession,
    ) {
        override fun toString(): String = description
    }

    data class ErrorScenario(
        val description: String,
        val username: String,
        val password: String,
        val mac: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
