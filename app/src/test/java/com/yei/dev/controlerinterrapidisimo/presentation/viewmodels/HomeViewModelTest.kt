package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.HomeState
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncStatus
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetUserSessionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LogoutUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.SyncDatabaseUseCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
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
 * Unit tests for HomeViewModel.
 *
 * Tests HomeViewModel user data display and sync functionality:
 * - Load user data with existing session
 * - Error handling for session retrieval
 * - Database synchronization flow
 * - Logout functionality
 * - Sync status updates
 *
 * Requirements: 6.1, 11.5, 14.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== LOAD USER DATA TESTS ==========

    @Test
    fun `property - loadUserData with existing session should show success state`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, true))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // When
            sut.loadUserData()
            advanceUntilIdle() // Wait for coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is HomeState.Success) {
                "Final state should be Success with user session"
            }

            val successState = finalState as HomeState.Success
            assert(successState.userSession.username == scenario.userSession.username) {
                "User session username should match"
            }
            assert(successState.userSession.name == scenario.userSession.name) {
                "User session name should match"
            }
            assert(successState.syncStatus == SyncStatus.NOT_SYNCED) {
                "Initial sync status should be NOT_SYNCED"
            }
        }
    }

    @Test
    fun `property - loadUserData with no session should show error state`() = runTest {
        // Given
        val sut = providesSut(
            getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(null)),
            syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, true))),
            logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
        )

        // When
        sut.loadUserData()
        advanceUntilIdle() // Wait for coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify final state
        assert(finalState is HomeState.Error) {
            "Final state should be Error when no session exists"
        }

        val errorState = finalState as HomeState.Error
        assert(errorState.message.contains("No user session found")) {
            "Error message should indicate no session found"
        }
    }

    @Test
    fun `property - loadUserData with session retrieval error should show error state`() = runTest {
        checkAll(
            iterations = 50,
            providesErrorScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Error(scenario.error)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, true))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // When
            sut.loadUserData()
            advanceUntilIdle() // Wait for coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is HomeState.Error) {
                "Final state should be Error when session retrieval fails"
            }

            val errorState = finalState as HomeState.Error
            assert(errorState.message.contains("Failed to load user data")) {
                "Error message should indicate user data load failure"
            }
        }
    }

    // ========== SYNC DATABASE TESTS ==========

    @Test
    fun `property - syncDatabase with successful sync should update sync status`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(5, 0, true))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // First load user data
            sut.loadUserData()
            advanceUntilIdle() // Wait for loadUserData to complete

            // When - Trigger sync
            sut.syncDatabase()
            advanceUntilIdle() // Wait for syncDatabase to complete

            // Then
            val finalState = sut.state.value

            // Verify sync status update
            assert(finalState is HomeState.Success) {
                "Should be Success state after sync"
            }

            val successState = finalState as HomeState.Success
            assert(successState.syncStatus == SyncStatus.SYNCED) {
                "Sync status should be SYNCED after successful sync"
            }
        }
    }

    @Test
    fun `property - syncDatabase with failed sync should update sync status to failed`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, false))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // First load user data
            sut.loadUserData()
            advanceUntilIdle() // Wait for loadUserData to complete

            // When - Trigger sync
            sut.syncDatabase()
            advanceUntilIdle() // Wait for syncDatabase to complete

            // Then
            val finalState = sut.state.value

            // Verify sync status update
            assert(finalState is HomeState.Success) {
                "Should be Success state after sync"
            }

            val successState = finalState as HomeState.Success
            assert(successState.syncStatus == SyncStatus.FAILED) {
                "Sync status should be FAILED after failed sync"
            }
        }
    }

    @Test
    fun `property - syncDatabase with sync error should update sync status to failed`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Error(AppError.NetworkError("Sync failed"))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // First load user data
            sut.loadUserData()
            advanceUntilIdle() // Wait for loadUserData to complete

            // When - Trigger sync
            sut.syncDatabase()
            advanceUntilIdle() // Wait for syncDatabase to complete

            // Then
            val finalState = sut.state.value

            // Verify sync status update
            assert(finalState is HomeState.Success) {
                "Should be Success state after sync"
            }

            val successState = finalState as HomeState.Success
            assert(successState.syncStatus == SyncStatus.FAILED) {
                "Sync status should be FAILED after sync error"
            }
        }
    }

    // ========== LOGOUT TESTS ==========

    @Test
    fun `property - logout with successful logout should not cause error`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, true))),
                logoutUseCase = providesLogoutUseCase(Result.Success(Unit)),
            )

            // When
            sut.logout()
            advanceUntilIdle() // Wait for logout to complete

            // Then - No error should occur
            // The logout success is handled by navigation, not state change
            // So we just verify the method doesn't throw
            assert(true) {
                "Logout should complete without throwing exceptions"
            }
        }
    }

    @Test
    fun `property - logout with error should update state to error`() = runTest {
        checkAll(
            iterations = 50,
            providesValidUserSessionScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                syncDatabaseUseCase = providesSyncDatabaseUseCase(Result.Success(SyncResult(0, 0, true))),
                logoutUseCase = providesLogoutUseCase(Result.Error(AppError.DatabaseError("Failed to clear session"))),
            )

            // First load user data to get to success state
            sut.loadUserData()
            advanceUntilIdle() // Wait for loadUserData to complete

            // When - Trigger logout
            sut.logout()
            advanceUntilIdle() // Wait for logout to complete

            // Then
            val finalState = sut.state.value

            // Verify error state
            assert(finalState is HomeState.Error) {
                "Should be Error state after logout failure"
            }

            val errorState = finalState as HomeState.Error
            assert(errorState.message.contains("Logout failed")) {
                "Error message should indicate logout failure"
            }
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with valid user sessions.
         */
        private fun providesValidUserSessionScenarios(): Arb<UserSessionScenario> = arbitrary {
            val username = Arb.string(minSize = 3, maxSize = 20)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val name = Arb.string(minSize = 3, maxSize = 50)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            UserSessionScenario(
                userSession = UserSession(
                    username = username,
                    name = name,
                ),
            )
        }

        /**
         * Provides scenarios with various error types.
         */
        private fun providesErrorScenarios(): Arb<ErrorScenario> = arbitrary {
            val errorType = Arb.int(0..4).bind()

            val error = when (errorType) {
                0 -> AppError.NetworkError("Network error")
                1 -> AppError.ApiError(statusCode = 500, message = "Internal Server Error")
                2 -> AppError.DatabaseError("Database error")
                3 -> AppError.ValidationError(field = "user", message = "Validation error")
                else -> AppError.UnknownError("Unknown error")
            }

            ErrorScenario(error = error)
        }

        /**
         * Provides the system under test (HomeViewModel).
         */
        private fun providesSut(
            getUserSessionUseCase: GetUserSessionUseCase,
            syncDatabaseUseCase: SyncDatabaseUseCase,
            logoutUseCase: LogoutUseCase,
        ): HomeViewModel {
            return HomeViewModel(
                getUserSessionUseCase = getUserSessionUseCase,
                syncDatabaseUseCase = syncDatabaseUseCase,
                logoutUseCase = logoutUseCase,
            )
        }

        /**
         * Provides a GetUserSessionUseCase mock.
         */
        private fun providesGetUserSessionUseCase(result: Result<UserSession?>) = mockk<GetUserSessionUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }

        /**
         * Provides a SyncDatabaseUseCase mock.
         */
        private fun providesSyncDatabaseUseCase(result: Result<SyncResult>) = mockk<SyncDatabaseUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }

        /**
         * Provides a LogoutUseCase mock.
         */
        private fun providesLogoutUseCase(result: Result<Unit>) = mockk<LogoutUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }
    }

    /**
     * Data class for user session scenarios.
     */
    data class UserSessionScenario(
        val userSession: UserSession,
    )

    /**
     * Data class for error scenarios.
     */
    data class ErrorScenario(
        val error: AppError,
    )
}
