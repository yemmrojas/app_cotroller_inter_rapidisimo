package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for CheckVersionUseCase.
 *
 * Tests verify that the use case correctly delegates to the VersionRepository
 * and returns the expected results for various scenarios.
 */
class CheckVersionUseCaseTest {

    @Test
    fun `invoke should return success with VersionStatus when repository succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesVersionRepository(
            result = Result.Success(scenario.expectedVersionStatus),
        )
        val sut = providesSut(
            versionRepository = mockRepository,
            appVersion = scenario.appVersion,
        )

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedVersionStatus)
        }
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val scenario = providesErrorScenarios().first()
        val mockRepository = providesVersionRepository(
            Result.Error(scenario.error),
        )
        val sut = providesSut(
            versionRepository = mockRepository,
            appVersion = scenario.appVersion,
        )

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
                description = "App is up to date",
                appVersion = "1.0.0",
                expectedVersionStatus = VersionStatus(
                    localVersion = "1.0.0",
                    apiVersion = "1.0.0",
                    status = VersionComparisonStatus.UP_TO_DATE,
                ),
            ),
            SuccessScenario(
                description = "App needs update",
                appVersion = "1.0.0",
                expectedVersionStatus = VersionStatus(
                    localVersion = "1.0.0",
                    apiVersion = "2.0.0",
                    status = VersionComparisonStatus.UPDATE_NEEDED,
                ),
            ),
            SuccessScenario(
                description = "App is ahead of server",
                appVersion = "2.0.0",
                expectedVersionStatus = VersionStatus(
                    localVersion = "2.0.0",
                    apiVersion = "1.0.0",
                    status = VersionComparisonStatus.AHEAD_OF_SERVER,
                ),
            ),
            SuccessScenario(
                description = "App version with multiple parts",
                appVersion = "1.2.3",
                expectedVersionStatus = VersionStatus(
                    localVersion = "1.2.3",
                    apiVersion = "1.2.4",
                    status = VersionComparisonStatus.UPDATE_NEEDED,
                ),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Network error",
                appVersion = "1.0.0",
                error = AppError.NetworkError("No internet connection"),
            ),
            ErrorScenario(
                description = "API error",
                appVersion = "1.0.0",
                error = AppError.ApiError(500, "Internal server error"),
            ),
            ErrorScenario(
                description = "Unknown error",
                appVersion = "1.0.0",
                error = AppError.UnknownError("Unexpected error occurred"),
            ),
        )

        private fun providesSut(
            versionRepository: VersionRepository,
            appVersion: String,
        ): CheckVersionUseCase =
            CheckVersionUseCase(
                versionRepository = versionRepository,
                appVersion = appVersion,
            )

        private fun providesVersionRepository(
            result: Result<VersionStatus>,
        ): VersionRepository {
            return mockk<VersionRepository> {
                every { checkVersion(any()) } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val appVersion: String,
        val expectedVersionStatus: VersionStatus,
    ) {
        override fun toString(): String = description
    }

    data class ErrorScenario(
        val description: String,
        val appVersion: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
