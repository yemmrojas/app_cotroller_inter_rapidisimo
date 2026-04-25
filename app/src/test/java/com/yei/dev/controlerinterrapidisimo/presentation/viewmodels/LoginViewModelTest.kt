package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.LoginState
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LoginUserUseCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginViewModel.
 *
 * Tests LoginViewModel authentication flow:
 * - Successful login with valid credentials
 * - Login failure with invalid credentials
 * - Network error handling
 * - API error handling
 * - State transitions (Idle → Loading → Success/Error)
 *
 * Requirements: 2.1, 2.4, 2.6, 14.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== LOGIN TESTS ==========

    @Test
    fun `property - login with valid credentials should transition to success state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidLoginScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(Result.Success(scenario.userSession)),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Success) {
                "Final state should be Success for valid credentials"
            }
        }
    }

    @Test
    fun `property - login with network error should transition to error state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(
                    Result.Error(AppError.NetworkError("No internet connection")),
                ),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Error) {
                "Final state should be Error for network failure"
            }

            val errorState = finalState as LoginState.Error
            assert(errorState.message.contains("Network error")) {
                "Error message should indicate network error"
            }
        }
    }

    @Test
    fun `property - login with API error should transition to error state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(
                    Result.Error(AppError.ApiError(statusCode = 401, message = "Unauthorized")),
                ),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Error) {
                "Final state should be Error for API failure"
            }

            val errorState = finalState as LoginState.Error
            assert(errorState.message.contains("Authentication failed")) {
                "Error message should indicate authentication failure"
            }
        }
    }

    @Test
    fun `property - login with validation error should transition to error state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(
                    Result.Error(AppError.ValidationError(field = "credentials", message = "Invalid input")),
                ),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Error) {
                "Final state should be Error for validation failure"
            }

            val errorState = finalState as LoginState.Error
            assert(errorState.message.contains("Invalid input")) {
                "Error message should indicate validation error"
            }
        }
    }

    @Test
    fun `property - login with database error should transition to error state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(
                    Result.Error(AppError.DatabaseError("Failed to save session")),
                ),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Error) {
                "Final state should be Error for database failure"
            }

            val errorState = finalState as LoginState.Error
            assert(errorState.message.contains("Database error")) {
                "Error message should indicate database error"
            }
        }
    }

    @Test
    fun `property - login with unknown error should transition to error state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidCredentialsScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                loginUserUseCase = providesLoginUserUseCase(
                    Result.Error(AppError.UnknownError("Unexpected error")),
                ),
            )

            // When
            sut.login(scenario.username, scenario.password)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is LoginState.Error) {
                "Final state should be Error for unknown failure"
            }

            val errorState = finalState as LoginState.Error
            assert(errorState.message.contains("Unexpected error")) {
                "Error message should indicate unexpected error"
            }
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with valid credentials.
         */
        private fun providesValidCredentialsScenarios(): Arb<CredentialsScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val password = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            CredentialsScenario(
                username = username,
                password = password,
            )
        }

        /**
         * Provides scenarios with valid login data and expected session.
         */
        private fun providesValidLoginScenarios(): Arb<ValidLoginScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val password = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            ValidLoginScenario(
                username = username,
                password = password,
                userSession = UserSession(
                    username = username,
                    name = name,
                ),
            )
        }

        /**
         * Provides the system under test (LoginViewModel).
         */
        private fun providesSut(
            loginUserUseCase: LoginUserUseCase,
        ): LoginViewModel {
            return LoginViewModel(
                loginUserUseCase = loginUserUseCase,
            )
        }

        /**
         * Provides a LoginUserUseCase mock.
         */
        private fun providesLoginUserUseCase(result: Result<UserSession>) = mockk<LoginUserUseCase>().apply {
            coEvery { this@apply.invoke(any<String>(), any<String>(), any<String>()) } returns flowOf(result)
        }
    }

    /**
     * Data class for credentials scenarios.
     */
    data class CredentialsScenario(
        val username: String,
        val password: String,
    )

    /**
     * Data class for valid login scenarios.
     */
    data class ValidLoginScenario(
        val username: String,
        val password: String,
        val userSession: UserSession,
    )
}
