package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for GetTablesUseCase.
 *
 * Tests verify that the use case correctly delegates to the DataSyncRepository
 * and returns the expected results for various scenarios.
 */
class GetTablesUseCaseTest {

    @Test
    fun `invoke should return success with table list when retrieval succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesDataSyncRepository(
            result = Result.Success(scenario.expectedTables),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        sut().collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedTables)
        }
    }

    @Test
    fun `invoke should return error when retrieval fails`() = runTest {
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
                description = "Multiple tables with records",
                expectedTables = listOf(
                    TableInfo(name = "users", recordCount = 100),
                    TableInfo(name = "products", recordCount = 250),
                    TableInfo(name = "orders", recordCount = 500),
                ),
            ),
            SuccessScenario(
                description = "Single table",
                expectedTables = listOf(
                    TableInfo(name = "settings", recordCount = 1),
                ),
            ),
            SuccessScenario(
                description = "Empty table list",
                expectedTables = emptyList(),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Database error retrieving tables",
                error = AppError.DatabaseError("Failed to retrieve tables"),
            ),
            ErrorScenario(
                description = "Unknown error",
                error = AppError.UnknownError("Unexpected error occurred"),
            ),
        )

        private fun providesSut(
            dataSyncRepository: DataSyncRepository,
        ): GetTablesUseCase =
            GetTablesUseCase(dataSyncRepository = dataSyncRepository)

        private fun providesDataSyncRepository(
            result: Result<List<TableInfo>>,
        ): DataSyncRepository {
            return mockk<DataSyncRepository> {
                every { getAllTables() } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val expectedTables: List<TableInfo>,
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
