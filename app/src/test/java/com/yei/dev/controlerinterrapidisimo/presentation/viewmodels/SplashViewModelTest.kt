package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SplashState
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import com.yei.dev.controlerinterrapidisimo.domain.usecases.CheckVersionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetUserSessionUseCase
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
 * Unit tests for SplashViewModel.
 *
 * Tests SplashViewModel state transitions:
 * - Version check success with session restoration
 * - Version check success without session
 * - Version mismatch scenarios
 * - Error handling for version check and session retrieval
 *
 * Requirements: 1.1, 1.2, 1.6, 1.7, 1.8, 14.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== CHECK VERSION AND SESSION TESTS ==========

    @Test
    fun `property - checkVersionAndSession with current version and existing session should navigate to home`() =
        runTest {
            checkAll(
                iterations = 50,
                providesValidUserSessionScenarios(),
            ) { scenario ->
                // Given
                val versionStatus = VersionStatus(
                    localVersion = "1.0.0",
                    apiVersion = "1.0.0",
                    status = VersionComparisonStatus.UP_TO_DATE,
                )

                val sut = providesSut(
                    checkVersionUseCase = providesCheckVersionUseCase(Result.Success(versionStatus)),
                    getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(scenario.userSession)),
                )

                // When
                sut.checkVersionAndSession()
                advanceUntilIdle() // NECESARIO: Ejecuta las coroutines

                // Then
                val finalState = sut.state.value

                // Verify navigation to home
                assert(finalState is SplashState.NavigateToHome) {
                    "Should navigate to home when version is current and session exists. State was: $finalState"
                }
            }
        }

    @Test
    fun `property - checkVersionAndSession with current version and no session should navigate to login`() = runTest {
        checkAll(
            iterations = 50,
            providesVersionScenarios(),
        ) { scenario ->
            // Given
            val versionStatus = VersionStatus(
                localVersion = scenario.localVersion,
                apiVersion = scenario.apiVersion,
                status = VersionComparisonStatus.UP_TO_DATE,
            )

            val sut = providesSut(
                checkVersionUseCase = providesCheckVersionUseCase(Result.Success(versionStatus)),
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(null)),
            )

            // When
            sut.checkVersionAndSession()
            advanceUntilIdle() // NECESARIO: Ejecuta las coroutines

            // Then
            val finalState = sut.state.value

            // Verify navigation to login
            assert(finalState is SplashState.NavigateToLogin) {
                "Should navigate to login when version is current and no session exists"
            }
        }
    }

    @Test
    fun `property - checkVersionAndSession with version mismatch should show version mismatch state`() = runTest {
        checkAll(
            iterations = 50,
            providesVersionMismatchScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                checkVersionUseCase = providesCheckVersionUseCase(Result.Success(scenario.versionStatus)),
                getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(null)),
            )

            // When
            sut.checkVersionAndSession()
            advanceUntilIdle() // NECESARIO: Ejecuta las coroutines

            // Then
            val finalState = sut.state.value

            // Verify version mismatch state
            assert(finalState is SplashState.VersionMismatch) {
                "Should show version mismatch when versions don't match"
            }
            val versionMismatchState = finalState as SplashState.VersionMismatch
            // Check that the message contains the expected keyword
            // Note: The actual message format is defined in SplashViewModel.MESSAGE_UPDATE_NEEDED
            // or SplashViewModel.MESSAGE_AHEAD_OF_SERVER
            assert(versionMismatchState.message.contains(scenario.expectedMessageKeyword)) {
                "Error message should contain '${scenario.expectedMessageKeyword}', got: '${versionMismatchState.message}'"
            }
        }
    }

    @Test
    fun `checkVersionAndSession with version check error should show error state`() = runTest {
        // Given
        val sut = providesSut(
            checkVersionUseCase = providesCheckVersionUseCase(Result.Error(AppError.NetworkError("No internet connection"))),
            getUserSessionUseCase = providesGetUserSessionUseCase(Result.Success(null)),
        )

        // When
        sut.checkVersionAndSession()
        advanceUntilIdle() // Wait for all coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify error state
        assert(finalState is SplashState.Error) {
            "Should show error when version check fails"
        }
        val errorState = finalState as SplashState.Error
        assert(errorState.message.contains("Version check failed")) {
            "Error message should indicate version check failure"
        }
    }

    @Test
    fun `checkVersionAndSession with session retrieval error should show error state`() = runTest {
        // Given
        val versionStatus = VersionStatus(
            localVersion = "1.0.0",
            apiVersion = "1.0.0",
            status = VersionComparisonStatus.UP_TO_DATE,
        )

        val sut = providesSut(
            checkVersionUseCase = providesCheckVersionUseCase(Result.Success(versionStatus)),
            getUserSessionUseCase = providesGetUserSessionUseCase(Result.Error(AppError.DatabaseError("Failed to retrieve session"))),
        )

        // When
        sut.checkVersionAndSession()
        advanceUntilIdle() // Wait for all coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify error state
        assert(finalState is SplashState.Error) {
            "Should show error when session retrieval fails"
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
         * Provides scenarios with version strings.
         */
        private fun providesVersionScenarios(): Arb<VersionScenario> = arbitrary {
            val major = Arb.int(1..10).bind()
            val minor = Arb.int(0..20).bind()
            val patch = Arb.int(0..50).bind()

            val localVersion = "$major.$minor.$patch"
            val apiVersion = "$major.$minor.$patch"  // Same version for UP_TO_DATE

            VersionScenario(
                localVersion = localVersion,
                apiVersion = apiVersion,
            )
        }

        /**
         * Provides scenarios with version mismatches.
         */
        private fun providesVersionMismatchScenarios(): Arb<VersionMismatchScenario> = arbitrary {
            val localMajor = Arb.int(1..10).bind()
            val localMinor = Arb.int(0..20).bind()
            val localPatch = Arb.int(0..50).bind()

            val apiMajor = Arb.int(1..10).bind()
            val apiMinor = Arb.int(0..20).bind()
            val apiPatch = Arb.int(0..50).bind()

            val localVersion = "$localMajor.$localMinor.$localPatch"
            val apiVersion = "$apiMajor.$apiMinor.$apiPatch"

            val status = if (localMajor < apiMajor ||
                (localMajor == apiMajor && localMinor < apiMinor) ||
                (localMajor == apiMajor && localMinor == apiMinor && localPatch < apiPatch)
            ) {
                VersionComparisonStatus.UPDATE_NEEDED
            } else {
                VersionComparisonStatus.AHEAD_OF_SERVER
            }

            val expectedMessageKeyword = if (status == VersionComparisonStatus.UPDATE_NEEDED) {
                "Update needed"
            } else {
                "Version ahead"
            }

            VersionMismatchScenario(
                versionStatus = VersionStatus(
                    localVersion = localVersion,
                    apiVersion = apiVersion,
                    status = status,
                ),
                expectedMessageKeyword = expectedMessageKeyword,
            )
        }

        /**
         * Provides the system under test (SplashViewModel).
         */
        private fun providesSut(
            checkVersionUseCase: CheckVersionUseCase,
            getUserSessionUseCase: GetUserSessionUseCase,
        ): SplashViewModel {
            return SplashViewModel(
                checkVersionUseCase = checkVersionUseCase,
                getUserSessionUseCase = getUserSessionUseCase,
            )
        }

        /**
         * Provides a CheckVersionUseCase mock.
         */
        private fun providesCheckVersionUseCase(result: Result<VersionStatus>) = mockk<CheckVersionUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }

        /**
         * Provides a GetUserSessionUseCase mock.
         */
        private fun providesGetUserSessionUseCase(result: Result<UserSession?>) = mockk<GetUserSessionUseCase>().apply {
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
     * Data class for version scenarios.
     */
    data class VersionScenario(
        val localVersion: String,
        val apiVersion: String,
    )

    /**
     * Data class for version mismatch scenarios.
     */
    data class VersionMismatchScenario(
        val versionStatus: VersionStatus,
        val expectedMessageKeyword: String,
    )
}
