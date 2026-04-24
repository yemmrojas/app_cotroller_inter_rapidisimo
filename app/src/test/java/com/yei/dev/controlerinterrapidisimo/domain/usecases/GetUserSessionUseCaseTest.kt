package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for GetUserSessionUseCase.
 *
 * Tests verify that the use case correctly delegates to the AuthRepository
 * and returns the expected results for various scenarios.
 */
class GetUserSessionUseCaseTest {

    @Test
    fun `invoke should return success with UserSession when session exists`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesAuthRepository(
            result = Result.Success(scenario.expectedUserSession),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedUserSession)
        }
    }

    @Test
    fun `invoke should return success with null when no session exists`() = runTest {
        // Given
        val mockRepository = providesAuthRepository(
            result = Result.Success(null),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertNull((result as Result.Success).data)
        }
    }

    @Test
    fun `invoke should return error when retrieval fails`() = runTest {
        // Given
        val scenario = providesErrorScenarios().first()
        val mockRepository = providesAuthRepository(
            result = Result.Error(scenario.error),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Error)
            assertEquals((result as Result.Error).error, scenario.error)
        }
    }

    // Provider methods
    companion object {
        fun providesSuccessScenarios() = listOf(
            SuccessScenario(
                description = "Existing user session",
                expectedUserSession = UserSession(
                    username = "testuser",
                    identification = "123456789",
                    name = "Test User",
                ),
            ),
            SuccessScenario(
                description = "Different user session",
                expectedUserSession = UserSession(
                    username = "admin",
                    identification = "987654321",
                    name = "Admin User",
                ),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Database error retrieving session",
                error = AppError.DatabaseError("Failed to retrieve session"),
            ),
            ErrorScenario(
                description = "Unknown error",
                error = AppError.UnknownError("Unexpected error occurred"),
            ),
        )

        private fun providesSut(
            authRepository: AuthRepository,
        ): GetUserSessionUseCase =
            GetUserSessionUseCase(authRepository = authRepository)

        private fun providesAuthRepository(
            result: Result<UserSession?>,
        ): AuthRepository {
            return mockk<AuthRepository> {
                every { getUserSession() } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val expectedUserSession: UserSession,
    ) {
        override fun toString(): String = description
    }

    data class ErrorScenario(
        val description: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
