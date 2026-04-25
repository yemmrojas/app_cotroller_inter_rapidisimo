package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.models.TablesState
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTableDataUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTablesUseCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
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
 * Unit tests for TablesViewModel.
 *
 * Tests TablesViewModel table operations:
 * - Loading tables list
 * - Loading table data for specific table
 * - Error handling for table operations
 * - Empty state handling
 *
 * Requirements: 7.1, 7.2, 7.5, 14.3
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TablesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== LOAD TABLES TESTS ==========

    @Test
    fun `property - loadTables with tables available should show tables list`() = runTest {
        checkAll(
            iterations = 50,
            providesTablesListScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getTablesUseCase = providesGetTablesUseCase(Result.Success(scenario.tables)),
                getTableDataUseCase = providesGetTableDataUseCase(Result.Success(emptyList())),
            )

            // When
            sut.loadTables()
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is TablesState.TablesList) {
                "Final state should be TablesList when tables are available"
            }

            val tablesListState = finalState as TablesState.TablesList
            assert(tablesListState.tables.size == scenario.tables.size) {
                "Should show all tables: expected ${scenario.tables.size}, got ${tablesListState.tables.size}"
            }
        }
    }

    @Test
    fun `property - loadTables with empty tables list should show error state`() = runTest {
        // Given
        val sut = providesSut(
            getTablesUseCase = providesGetTablesUseCase(Result.Success(emptyList())),
            getTableDataUseCase = providesGetTableDataUseCase(Result.Success(emptyList())),
        )

        // When
        sut.loadTables()
        advanceUntilIdle() // Wait for all coroutines to complete

        // Then
        val finalState = sut.state.value

        // Verify final state
        assert(finalState is TablesState.Error) {
            "Final state should be Error when no tables available"
        }

        val errorState = finalState as TablesState.Error
        assert(errorState.message.contains("No tables available")) {
            "Error message should indicate no tables available"
        }
    }

    @Test
    fun `property - loadTables with error should show error state`() = runTest {
        checkAll(
            iterations = 50,
            providesErrorScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getTablesUseCase = providesGetTablesUseCase(Result.Error(scenario.error)),
                getTableDataUseCase = providesGetTableDataUseCase(Result.Success(emptyList())),
            )

            // When
            sut.loadTables()
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is TablesState.Error) {
                "Final state should be Error when table loading fails"
            }

            val errorState = finalState as TablesState.Error
            assert(errorState.message.contains("Failed to load tables")) {
                "Error message should indicate table load failure"
            }
        }
    }

    // ========== LOAD TABLE DATA TESTS ==========

    @Test
    fun `property - loadTableData with table data available should show table data state`() = runTest {
        checkAll(
            iterations = 50,
            providesTableDataScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getTablesUseCase = providesGetTablesUseCase(Result.Success(emptyList())),
                getTableDataUseCase = providesGetTableDataUseCase(Result.Success(scenario.tableData)),
            )

            // When
            sut.loadTableData(scenario.tableName)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is TablesState.TableData) {
                "Final state should be TableData when table data is available"
            }

            val tableDataState = finalState as TablesState.TableData
            assert(tableDataState.tableName == scenario.tableName) {
                "Table name should match: expected '${scenario.tableName}', got '${tableDataState.tableName}'"
            }
            assert(tableDataState.data.size == scenario.tableData.size) {
                "Should show all table data: expected ${scenario.tableData.size}, got ${tableDataState.data.size}"
            }
        }
    }

    @Test
    fun `property - loadTableData with empty table data should show error state`() = runTest {
        checkAll(
            iterations = 50,
            providesTableNameScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getTablesUseCase = providesGetTablesUseCase(Result.Success(emptyList())),
                getTableDataUseCase = providesGetTableDataUseCase(Result.Success(emptyList())),
            )

            // When
            sut.loadTableData(scenario.tableName)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is TablesState.Error) {
                "Final state should be Error when table data is empty"
            }

            val errorState = finalState as TablesState.Error
            assert(errorState.message.contains("Table '${scenario.tableName}' is empty")) {
                "Error message should indicate table is empty"
            }
        }
    }

    @Test
    fun `property - loadTableData with error should show error state`() = runTest {
        checkAll(
            iterations = 50,
            providesTableNameScenarios(),
        ) { scenario ->
            // Given
            val sut = providesSut(
                getTablesUseCase = providesGetTablesUseCase(Result.Success(emptyList())),
                getTableDataUseCase = providesGetTableDataUseCase(Result.Error(AppError.DatabaseError("Failed to load table data"))),
            )

            // When
            sut.loadTableData(scenario.tableName)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            val finalState = sut.state.value

            // Verify final state
            assert(finalState is TablesState.Error) {
                "Final state should be Error when table data loading fails"
            }

            val errorState = finalState as TablesState.Error
            assert(errorState.message.contains("Failed to load table data")) {
                "Error message should indicate table data load failure"
            }
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with tables list.
         */
        private fun providesTablesListScenarios(): Arb<TablesListScenario> = arbitrary {
            val tableCount = Arb.int(1..10).bind()
            val tables = Arb.list(Arb.tableInfo(), tableCount..tableCount).bind()

            TablesListScenario(tables = tables)
        }

        /**
         * Provides scenarios with table names.
         */
        private fun providesTableNameScenarios(): Arb<TableNameScenario> = arbitrary {
            val tableName = Arb.string(minSize = 3, maxSize = 30)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            TableNameScenario(tableName = tableName)
        }

        /**
         * Provides scenarios with table data.
         */
        private fun providesTableDataScenarios(): Arb<TableDataScenario> = arbitrary {
            val tableName = Arb.string(minSize = 3, maxSize = 30)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val rowCount = Arb.int(1..10).bind()
            val tableData = List(rowCount) { rowIndex ->
                mapOf(
                    "id" to rowIndex,
                    "name" to "Item $rowIndex",
                    "value" to rowIndex * 10
                )
            }

            TableDataScenario(
                tableName = tableName,
                tableData = tableData
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
                3 -> AppError.ValidationError(field = "table", message = "Validation error")
                else -> AppError.UnknownError("Unknown error")
            }

            ErrorScenario(error = error)
        }

        /**
         * Provides a TableInfo generator.
         */
        private fun Arb.Companion.tableInfo(): Arb<TableInfo> = arbitrary {
            val name = Arb.string(minSize = 3, maxSize = 30)
                .filter { it.isNotBlank() && it.trim() == it }
                .bind()

            val recordCount = Arb.int(0..1000).bind()

            TableInfo(
                name = name,
                recordCount = recordCount
            )
        }

        /**
         * Provides the system under test (TablesViewModel).
         */
        private fun providesSut(
            getTablesUseCase: GetTablesUseCase,
            getTableDataUseCase: GetTableDataUseCase,
        ): TablesViewModel {
            return TablesViewModel(
                getTablesUseCase = getTablesUseCase,
                getTableDataUseCase = getTableDataUseCase,
            )
        }

        /**
         * Provides a GetTablesUseCase mock.
         */
        private fun providesGetTablesUseCase(result: Result<List<TableInfo>>) = mockk<GetTablesUseCase>().apply {
            coEvery { this@apply.invoke() } returns flowOf(result)
        }

        /**
         * Provides a GetTableDataUseCase mock.
         */
        private fun providesGetTableDataUseCase(result: Result<List<Map<String, Any>>>) = mockk<GetTableDataUseCase>().apply {
            coEvery { this@apply.invoke(any<String>()) } returns flowOf(result)
        }
    }

    /**
     * Data class for tables list scenarios.
     */
    data class TablesListScenario(
        val tables: List<TableInfo>,
    )

    /**
     * Data class for table name scenarios.
     */
    data class TableNameScenario(
        val tableName: String,
    )

    /**
     * Data class for table data scenarios.
     */
    data class TableDataScenario(
        val tableName: String,
        val tableData: List<Map<String, Any>>,
    )

    /**
     * Data class for error scenarios.
     */
    data class ErrorScenario(
        val error: AppError,
    )
}
