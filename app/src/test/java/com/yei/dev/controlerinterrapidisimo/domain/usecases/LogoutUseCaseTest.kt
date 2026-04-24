package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for LogoutUseCase.
 *
 * Tests verify that the use case correctly delegates to the AuthRepository
 * and returns the expected results for various scenarios.
 */
class LogoutUseCaseTest {

    @Test
    fun `invoke should return success when logout succeeds`() = runTest {
        // Given
        val mockRepository = providesAuthRepository(
            result = Result.Success(Unit),
        )
        val sut = providesSut(authRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, Unit)
        }
    }

    @Test
    fun `invoke should return error when logout fails`() = runTest {
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
        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Database error clearing session",
                error = AppError.DatabaseError("Failed to clear session"),
            ),
            ErrorScenario(
                description = "Unknown error during logout",
                error = AppError.UnknownError("Unexpected error occurred"),
            ),
        )

        private fun providesSut(
            authRepository: AuthRepository,
        ): LogoutUseCase =
            LogoutUseCase(authRepository = authRepository)

        private fun providesAuthRepository(
            result: Result<Unit>,
        ): AuthRepository {
            return mockk<AuthRepository> {
                every { clearUserSession() } returns flowOf(result)
            }
        }
    }

    data class ErrorScenario(
        val description: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
