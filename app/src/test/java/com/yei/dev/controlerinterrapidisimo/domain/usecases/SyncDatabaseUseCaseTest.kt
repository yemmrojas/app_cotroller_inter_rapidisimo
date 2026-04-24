package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for SyncDatabaseUseCase.
 *
 * Tests verify that the use case correctly delegates to the DataSyncRepository
 * and returns the expected results for various scenarios.
 */
class SyncDatabaseUseCaseTest {

    @Test
    fun `invoke should return success with SyncResult when sync succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesDataSyncRepository(
            result = Result.Success(scenario.expectedSyncResult),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedSyncResult)
        }
    }

    @Test
    fun `invoke should return error when sync fails`() = runTest {
        // Given
        val scenario = providesErrorScenarios().first()
        val mockRepository = providesDataSyncRepository(
            result = Result.Error(scenario.error),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

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
                description = "Successful sync with tables created",
                expectedSyncResult = SyncResult(
                    tablesCreated = 5,
                    tablesUpdated = 0,
                    success = true,
                ),
            ),
            SuccessScenario(
                description = "Successful sync with tables updated",
                expectedSyncResult = SyncResult(
                    tablesCreated = 2,
                    tablesUpdated = 3,
                    success = true,
                ),
            ),
            SuccessScenario(
                description = "Successful sync with no changes",
                expectedSyncResult = SyncResult(
                    tablesCreated = 0,
                    tablesUpdated = 0,
                    success = true,
                ),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Network error during sync",
                error = AppError.NetworkError("No internet connection"),
            ),
            ErrorScenario(
                description = "API error fetching schema",
                error = AppError.ApiError(500, "Internal server error"),
            ),
            ErrorScenario(
                description = "Database error creating tables",
                error = AppError.DatabaseError("Failed to create tables"),
            ),
        )

        private fun providesSut(
            dataSyncRepository: DataSyncRepository,
        ): SyncDatabaseUseCase =
            SyncDatabaseUseCase(dataSyncRepository = dataSyncRepository)

        private fun providesDataSyncRepository(
            result: Result<SyncResult>,
        ): DataSyncRepository {
            return mockk<DataSyncRepository> {
                every { syncDatabase() } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val expectedSyncResult: SyncResult,
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
