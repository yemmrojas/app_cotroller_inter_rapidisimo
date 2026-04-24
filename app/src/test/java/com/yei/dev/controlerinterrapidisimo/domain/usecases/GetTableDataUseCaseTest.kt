package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for GetTableDataUseCase.
 *
 * Tests verify that the use case correctly delegates to the DataSyncRepository
 * and returns the expected results for various scenarios.
 */
class GetTableDataUseCaseTest {

    @Test
    fun `invoke should return success with table data when retrieval succeeds`() = runTest {
        // Given
        val scenario = providesSuccessScenarios().first()
        val mockRepository = providesDataSyncRepository(
            result = Result.Success(scenario.expectedData),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        sut(tableName = scenario.tableName).collect { result ->
            // Then
            assert(result is Result.Success)
            assertEquals((result as Result.Success).data, scenario.expectedData)
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
        sut(tableName = scenario.tableName).collect { result ->
            // Then
            assert(result is Result.Error)
            assertEquals((result as Result.Error).error, scenario.error)
        }
    }

    // Provider methods
    companion object {
        fun providesSuccessScenarios() = listOf(
            SuccessScenario(
                description = "Table with multiple rows",
                tableName = "users",
                expectedData = listOf(
                    mapOf("id" to 1, "name" to "John", "email" to "john@example.com"),
                    mapOf("id" to 2, "name" to "Jane", "email" to "jane@example.com"),
                ),
            ),
            SuccessScenario(
                description = "Table with single row",
                tableName = "settings",
                expectedData = listOf(
                    mapOf("key" to "theme", "value" to "dark"),
                ),
            ),
            SuccessScenario(
                description = "Empty table",
                tableName = "logs",
                expectedData = emptyList(),
            ),
            SuccessScenario(
                description = "Table with nullable columns (SQL NULL values)",
                tableName = "profiles",
                expectedData = listOf(
                    mapOf("id" to 1, "name" to "Alice", "bio" to null, "avatar" to "avatar1.png"),
                    mapOf("id" to 2, "name" to "Bob", "bio" to "Developer", "avatar" to null),
                ),
            ),
        )

        fun providesErrorScenarios() = listOf(
            ErrorScenario(
                description = "Database error retrieving data",
                tableName = "users",
                error = AppError.DatabaseError("Failed to retrieve table data"),
            ),
            ErrorScenario(
                description = "Table not found",
                tableName = "nonexistent",
                error = AppError.DatabaseError("Table does not exist"),
            ),
        )

        private fun providesSut(
            dataSyncRepository: DataSyncRepository,
        ): GetTableDataUseCase =
            GetTableDataUseCase(dataSyncRepository = dataSyncRepository)

        private fun providesDataSyncRepository(
            result: Result<List<Map<String, Any?>>>,
        ): DataSyncRepository {
            return mockk<DataSyncRepository> {
                every { getTableData(any()) } returns flowOf(result)
            }
        }
    }

    data class SuccessScenario(
        val description: String,
        val tableName: String,
        val expectedData: List<Map<String, Any?>>,
    ) {
        override fun toString(): String = description
    }

    data class ErrorScenario(
        val description: String,
        val tableName: String,
        val error: AppError,
    ) {
        override fun toString(): String = description
    }
}
