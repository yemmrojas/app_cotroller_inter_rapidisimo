package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for SyncDatabaseUseCase.
 *
 * Tests verify that the use case correctly orchestrates schema fetching
 * and table synchronization through the DataSyncRepository.
 */
class SyncDatabaseUseCaseTest {

    @Test
    fun `property - invoke should fetch schema and sync tables successfully`() = runTest {
        checkAll(
            iterations = 50,
            providesSchemaScenarios(),
        ) { scenario ->
            // Given
            val mockRepository = providesDataSyncRepository(
                schemaResult = Result.Success(scenario.schemas),
                syncResult = Result.Success(scenario.expectedSyncResult),
            )
            val sut = providesSut(dataSyncRepository = mockRepository)

            // When
            var result: Result<SyncResult>? = null
            sut().collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success when both fetch and sync succeed"
            }
            assertEquals((result as Result.Success).data, scenario.expectedSyncResult)
        }
    }

    @Test
    fun `invoke should return error when schema fetch fails`() = runTest {
        // Given
        val networkError = AppError.NetworkError("No internet connection")
        val mockRepository = providesDataSyncRepository(
            schemaResult = Result.Error(networkError),
            syncResult = Result.Success(SyncResult(0, 0, true)),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        var result: Result<SyncResult>? = null
        sut().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error when schema fetch fails"
        }
        assertEquals((result as Result.Error).error, networkError)
    }

    @Test
    fun `invoke should return error when table sync fails`() = runTest {
        // Given
        val schemas = listOf(
            TableSchema(
                tableName = "test_table",
                columns = listOf(
                    ColumnDefinition(
                        name = "id",
                        type = "INTEGER",
                        nullable = false,
                        primaryKey = true,
                    ),
                ),
            ),
        )
        val databaseError = AppError.DatabaseError("Failed to create tables")
        val mockRepository = providesDataSyncRepository(
            schemaResult = Result.Success(schemas),
            syncResult = Result.Error(databaseError),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        var result: Result<SyncResult>? = null
        sut().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error when table sync fails"
        }
        assertEquals((result as Result.Error).error, databaseError)
    }

    @Test
    fun `invoke with empty schema should sync successfully with zero counts`() = runTest {
        // Given
        val mockRepository = providesDataSyncRepository(
            schemaResult = Result.Success(emptyList()),
            syncResult = Result.Success(SyncResult(0, 0, true)),
        )
        val sut = providesSut(dataSyncRepository = mockRepository)

        // When
        var result: Result<SyncResult>? = null
        sut().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty schema"
        }
        val syncResult = (result as Result.Success).data
        assert(syncResult.tablesCreated == 0)
        assert(syncResult.tablesUpdated == 0)
        assert(syncResult.success)
    }

    // Provider methods
    companion object {
        /**
         * Provides scenarios with table schemas.
         */
        private fun providesSchemaScenarios(): Arb<SchemaScenario> = arbitrary {
            val count = Arb.int(1..5).bind()
            val schemas = List(count) {
                val tableName = Arb.string(minSize = 5, maxSize = 20)
                    .filter { it.isNotBlank() && it.matches(Regex("[a-z_]+")) }
                    .bind()

                val columnCount = Arb.int(1..5).bind()
                val columns = List(columnCount) { index ->
                    ColumnDefinition(
                        name = "column_$index",
                        type = "TEXT",
                        nullable = Arb.boolean().bind(),
                        primaryKey = index == 0,
                    )
                }

                TableSchema(
                    tableName = tableName,
                    columns = columns,
                )
            }

            val syncResult = SyncResult(
                tablesCreated = count,
                tablesUpdated = 0,
                success = true,
            )

            SchemaScenario(
                schemas = schemas,
                expectedSyncResult = syncResult,
            )
        }

        private fun providesSut(
            dataSyncRepository: DataSyncRepository,
        ): SyncDatabaseUseCase =
            SyncDatabaseUseCase(dataSyncRepository = dataSyncRepository)

        private fun providesDataSyncRepository(
            schemaResult: Result<List<TableSchema>>,
            syncResult: Result<SyncResult>,
        ): DataSyncRepository {
            return mockk<DataSyncRepository> {
                every { fetchDatabaseSchema() } returns flowOf(schemaResult)
                every { syncTables(any()) } returns flowOf(syncResult)
            }
        }
    }

    data class SchemaScenario(
        val schemas: List<TableSchema>,
        val expectedSyncResult: SyncResult,
    )
}
