package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.local.AppDatabase
import com.yei.dev.controlerinterrapidisimo.data.local.dao.DynamicTableDao
import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.DataSyncApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.SchemaResponseDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based and unit tests for DataSyncRepositoryImpl.
 *
 * Tests all methods of DataSyncRepositoryImpl:
 * - fetchDatabaseSchema() with various scenarios
 * - syncTables() with various scenarios
 * - getAllTables() with various scenarios
 * - getTableData() with various scenarios
 */
class DataSyncRepositoryImplTest {

    // ========== FETCH DATABASE SCHEMA TESTS ==========

    @Test
    fun `property - fetchDatabaseSchema should return list of table schemas`() = runTest {
        checkAll(
            iterations = 50,
            providesSchemaScenarios(),
        ) { scenario ->
            // Given
            // SchemaResponseDto is now a List<TableSchemaDto> directly
            val schemaDto: SchemaResponseDto = scenario.tableDtos
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(schemaDto)),
                database = providesAppDatabase(providesDynamicTableDao()),
                dynamicTableDao = providesDynamicTableDao(),
            )

            // When
            var result: Result<List<TableSchema>>? = null
            sut.fetchDatabaseSchema().collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for valid schema"
            }
            val schemas = (result as Result.Success).data
            assert(schemas.size == scenario.tableDtos.size) {
                "Should return ${scenario.tableDtos.size} schemas, got ${schemas.size}"
            }
            schemas.forEachIndexed { index, schema ->
                assert(schema.tableName == scenario.tableDtos[index].tableName) {
                    "Table name should match at index $index"
                }
                assert(schema.columns.size == scenario.tableDtos[index].columns.size) {
                    "Column count should match at index $index"
                }
            }
        }
    }

    @Test
    fun `fetchDatabaseSchema with network error should return network error`() = runTest {
        // Given
        val networkError = AppError.NetworkError("No internet connection")
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Error(networkError)),
            database = providesAppDatabase(providesDynamicTableDao()),
            dynamicTableDao = providesDynamicTableDao(),
        )

        // When
        var result: Result<List<TableSchema>>? = null
        sut.fetchDatabaseSchema().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for network failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.NetworkError) {
            "Should be NetworkError"
        }
    }

    @Test
    fun `fetchDatabaseSchema with empty schema should return empty list`() = runTest {
        // Given
        // SchemaResponseDto is now a List<TableSchemaDto> directly
        val schemaDto: SchemaResponseDto = emptyList()
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(schemaDto)),
            database = providesAppDatabase(providesDynamicTableDao()),
            dynamicTableDao = providesDynamicTableDao(),
        )

        // When
        var result: Result<List<TableSchema>>? = null
        sut.fetchDatabaseSchema().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty schema"
        }
        val schemas = (result as Result.Success).data
        assert(schemas.isEmpty()) {
            "Should return empty list"
        }
    }

    // ========== SYNC TABLES TESTS ==========

    @Test
    fun `property - syncTables with new tables should create tables and return success`() = runTest {
        checkAll(
            iterations = 50,
            providesNewTablesScenarios(),
        ) { scenario ->
            // Given
            val mockDao = providesDynamicTableDao(existingTables = emptyList())
            val mockDatabase = providesAppDatabase(mockDao)

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                database = mockDatabase,
                dynamicTableDao = mockDao,
            )

            // When
            var result: Result<SyncResult>? = null
            sut.syncTables(scenario.schemas).collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for valid schema"
            }
            val syncResult = (result as Result.Success).data
            assert(syncResult.success) {
                "Sync should be successful"
            }
            assert(syncResult.tablesCreated == scenario.schemas.size) {
                "Should create ${scenario.schemas.size} tables, got ${syncResult.tablesCreated}"
            }
            assert(syncResult.tablesUpdated == 0) {
                "Should not update any tables for new tables"
            }

            // Verify createTable was called for each table
            scenario.schemas.forEach { schema ->
                verify { mockDao.createTable(schema.tableName, any(), any()) }
            }
        }
    }

    @Test
    fun `property - syncTables with existing tables should update tables and return success`() = runTest {
        checkAll(
            iterations = 50,
            providesExistingTablesScenarios(),
        ) { scenario ->
            // Given
            val existingTableNames = scenario.schemas.map { it.tableName }
            val mockDao = providesDynamicTableDao(existingTables = existingTableNames)
            val mockDatabase = providesAppDatabase(mockDao)

            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                database = mockDatabase,
                dynamicTableDao = mockDao,
            )

            // When
            var result: Result<SyncResult>? = null
            sut.syncTables(scenario.schemas).collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success for existing tables"
            }
            val syncResult = (result as Result.Success).data
            assert(syncResult.success) {
                "Sync should be successful"
            }
            assert(syncResult.tablesCreated == 0) {
                "Should not create any tables for existing tables"
            }
            assert(syncResult.tablesUpdated == scenario.schemas.size) {
                "Should update ${scenario.schemas.size} tables, got ${syncResult.tablesUpdated}"
            }

            // Verify updateTableSchema was called for each table
            scenario.schemas.forEach { schema ->
                verify { mockDao.updateTableSchema(schema.tableName, any()) }
            }
        }
    }

    @Test
    fun `syncTables with database error should return database error`() = runTest {
        // Given
        val schema = TableSchema(
            tableName = "test_table",
            columns = listOf(
                ColumnDefinition(
                    name = "id",
                    type = "INTEGER",
                    nullable = false,
                    primaryKey = true,
                ),
            ),
        )
        val mockDao = providesDynamicTableDaoWithError()
        val mockDatabase = providesAppDatabase(mockDao)

        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = mockDatabase,
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<SyncResult>? = null
        sut.syncTables(listOf(schema)).collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for database failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
    }

    @Test
    fun `syncTables with empty schemas should return success with zero counts`() = runTest {
        // Given
        val mockDao = providesDynamicTableDao()
        val mockDatabase = providesAppDatabase(mockDao)

        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = mockDatabase,
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<SyncResult>? = null
        sut.syncTables(emptyList()).collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty schemas"
        }
        val syncResult = (result as Result.Success).data
        assert(syncResult.tablesCreated == 0) {
            "Should create 0 tables"
        }
        assert(syncResult.tablesUpdated == 0) {
            "Should update 0 tables"
        }
        assert(syncResult.success) {
            "Sync should be successful"
        }
    }

    @Test
    fun `syncTables with failed table creation should return error`() = runTest {
        // Given
        val schema = TableSchema(
            tableName = "test_table",
            columns = emptyList(), // Empty columns will cause createTable to return false
            queryCreacion = "", // No SQL provided
        )
        
        val mockDao = mockk<DynamicTableDao>(relaxed = true).apply {
            every { tableExists(any()) } returns false
            every { createTable(any(), any(), any()) } returns false // Simulate creation failure
        }
        val mockDatabase = providesAppDatabase(mockDao)

        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = mockDatabase,
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<SyncResult>? = null
        sut.syncTables(listOf(schema)).collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error when table creation fails"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
        assert(error.message.contains("Failed to create 1")) {
            "Error message should indicate 1 table failed"
        }
    }

    @Test
    fun `syncTables with partial failures should return error with correct counts`() = runTest {
        // Given
        val schemas = listOf(
            TableSchema(tableName = "table1", columns = listOf(ColumnDefinition("id", "INTEGER", false, true))),
            TableSchema(tableName = "table2", columns = emptyList()), // Will fail
            TableSchema(tableName = "table3", columns = listOf(ColumnDefinition("id", "INTEGER", false, true))),
        )
        
        val mockDao = mockk<DynamicTableDao>(relaxed = true).apply {
            every { tableExists(any()) } returns false
            every { createTable("table1", any(), any()) } returns true
            every { createTable("table2", any(), any()) } returns false // Fails
            every { createTable("table3", any(), any()) } returns true
        }
        val mockDatabase = providesAppDatabase(mockDao)

        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = mockDatabase,
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<SyncResult>? = null
        sut.syncTables(schemas).collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error when any table creation fails"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
        assert(error.message.contains("Failed to create 1 of 3 tables")) {
            "Error message should indicate 1 of 3 tables failed, got: ${error.message}"
        }
    }

    // ========== GET ALL TABLES TESTS ==========

    @Test
    fun `property - getAllTables should return list of table info`() = runTest {
        checkAll(
            iterations = 50,
            providesTableInfoScenarios(),
        ) { scenario ->
            // Given
            val mockDao = providesDynamicTableDaoWithTables(scenario.tables)
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                database = providesAppDatabase(mockDao),
                dynamicTableDao = mockDao,
            )

            // When
            var result: Result<List<TableInfo>>? = null
            sut.getAllTables().collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success"
            }
            val tables = (result as Result.Success).data
            assert(tables.size == scenario.tables.size) {
                "Should return ${scenario.tables.size} tables, got ${tables.size}"
            }
            tables.forEachIndexed { index, table ->
                assert(table.name == scenario.tables[index].name) {
                    "Table name should match at index $index"
                }
                assert(table.recordCount == scenario.tables[index].recordCount) {
                    "Record count should match at index $index"
                }
            }
        }
    }

    @Test
    fun `getAllTables with empty database should return empty list`() = runTest {
        // Given
        val mockDao = providesDynamicTableDaoWithTables(emptyList())
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = providesAppDatabase(mockDao),
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<List<TableInfo>>? = null
        sut.getAllTables().collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty database"
        }
        val tables = (result as Result.Success).data
        assert(tables.isEmpty()) {
            "Should return empty list"
        }
    }

    @Test
    fun `getAllTables with database error should return database error`() = runTest {
        // Given
        val mockDao = providesDynamicTableDaoWithGetTablesError()
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = providesAppDatabase(mockDao),
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<List<TableInfo>>? = null
        sut.getAllTables().collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for database failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
    }

    // ========== GET TABLE DATA TESTS ==========

    @Test
    fun `property - getTableData should return table data`() = runTest {
        checkAll(
            iterations = 50,
            providesTableDataScenarios(),
        ) { scenario ->
            // Given
            val mockDao = providesDynamicTableDaoWithData(scenario.tableName, scenario.data)
            val sut = providesSut(
                networkHandler = providesNetworkHandler(Result.Success(mockk())),
                database = providesAppDatabase(mockDao),
                dynamicTableDao = mockDao,
            )

            // When
            var result: Result<List<Map<String, Any?>>>? = null
            sut.getTableData(scenario.tableName).collect { result = it }

            // Then
            assert(result is Result.Success) {
                "Should return success"
            }
            val data = (result as Result.Success).data
            assert(data.size == scenario.data.size) {
                "Should return ${scenario.data.size} rows, got ${data.size}"
            }
        }
    }

    @Test
    fun `getTableData with empty table should return empty list`() = runTest {
        // Given
        val tableName = "empty_table"
        val mockDao = providesDynamicTableDaoWithData(tableName, emptyList())
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = providesAppDatabase(mockDao),
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<List<Map<String, Any?>>>? = null
        sut.getTableData(tableName).collect { result = it }

        // Then
        assert(result is Result.Success) {
            "Should return success even with empty table"
        }
        val data = (result as Result.Success).data
        assert(data.isEmpty()) {
            "Should return empty list"
        }
    }

    @Test
    fun `getTableData with database error should return database error`() = runTest {
        // Given
        val tableName = "error_table"
        val mockDao = providesDynamicTableDaoWithGetDataError()
        val sut = providesSut(
            networkHandler = providesNetworkHandler(Result.Success(mockk())),
            database = providesAppDatabase(mockDao),
            dynamicTableDao = mockDao,
        )

        // When
        var result: Result<List<Map<String, Any?>>>? = null
        sut.getTableData(tableName).collect { result = it }

        // Then
        assert(result is Result.Error) {
            "Should return error for database failure"
        }
        val error = (result as Result.Error).error
        assert(error is AppError.DatabaseError) {
            "Should be DatabaseError"
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides scenarios with table schemas.
         */
        private fun providesSchemaScenarios(): Arb<SchemaScenario> = arbitrary {
            val count = Arb.int(1..5).bind()
            val tables = List(count) {
                val tableName = Arb.string(minSize = 5, maxSize = 20)
                    .filter { it.isNotBlank() && it.matches(Regex("[a-z_]+")) }
                    .bind()

                val columnCount = Arb.int(1..5).bind()
                val columns = List(columnCount) { index ->
                    ColumnDefinitionDto(
                        name = "column_$index",
                        type = "TEXT",
                        nullable = Arb.boolean().bind(),
                        primaryKey = index == 0,
                    )
                }

                TableSchemaDto(
                    tableName = tableName,
                    columns = columns,
                )
            }

            SchemaScenario(tableDtos = tables)
        }

        /**
         * Provides scenarios with new tables.
         */
        private fun providesNewTablesScenarios(): Arb<TablesScenario> = arbitrary {
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

            TablesScenario(schemas = schemas)
        }

        /**
         * Provides scenarios with existing tables.
         */
        private fun providesExistingTablesScenarios(): Arb<TablesScenario> = arbitrary {
            val count = Arb.int(1..5).bind()
            val schemas = List(count) {
                val tableName = "existing_table_$it"
                val columns = listOf(
                    ColumnDefinition(
                        name = "id",
                        type = "INTEGER",
                        nullable = false,
                        primaryKey = true,
                    ),
                )

                TableSchema(
                    tableName = tableName,
                    columns = columns,
                )
            }

            TablesScenario(schemas = schemas)
        }

        /**
         * Provides scenarios with table info.
         */
        private fun providesTableInfoScenarios(): Arb<TableInfoScenario> = arbitrary {
            val count = Arb.int(1..10).bind()
            val tables = List(count) {
                val name = Arb.string(minSize = 5, maxSize = 20)
                    .filter { it.isNotBlank() }
                    .bind()
                val recordCount = Arb.int(0..1000).bind()

                TableInfo(
                    name = name,
                    recordCount = recordCount,
                )
            }

            TableInfoScenario(tables = tables)
        }

        /**
         * Provides scenarios with table data.
         */
        private fun providesTableDataScenarios(): Arb<TableDataScenario> = arbitrary {
            val tableName = Arb.string(minSize = 5, maxSize = 20)
                .filter { it.isNotBlank() }
                .bind()

            val rowCount = Arb.int(1..10).bind()
            val data = List(rowCount) {
                mapOf(
                    "id" to it,
                    "name" to "row_$it",
                )
            }

            TableDataScenario(
                tableName = tableName,
                data = data,
            )
        }

        /**
         * Provides the system under test (DataSyncRepositoryImpl).
         */
        private fun providesSut(
            networkHandler: NetworkHandler,
            database: AppDatabase,
            dynamicTableDao: DynamicTableDao,
        ): DataSyncRepositoryImpl {
            return DataSyncRepositoryImpl(
                apiService = providesApiService(),
                database = database,
                dynamicTableDao = dynamicTableDao,
                networkHandler = networkHandler,
                tableSchemaConverter = providesTableSchemaConverter(),
            )
        }

        /**
         * Provides a DataSyncApiService mock.
         */
        private fun providesApiService() = mockk<DataSyncApiService>()

        /**
         * Provides a NetworkHandler mock.
         */
        private fun providesNetworkHandler(result: Result<SchemaResponseDto>) = mockk<NetworkHandler>().apply {
            coEvery { safeApiCall<SchemaResponseDto>(any()) } returns result
        }

        /**
         * Provides an AppDatabase mock.
         */
        private fun providesAppDatabase(dao: DynamicTableDao) = mockk<AppDatabase>(relaxed = true).apply {
            every { runInTransaction(any()) } answers {
                val block = firstArg<Runnable>()
                block.run()
            }
        }

        /**
         * Provides a DynamicTableDao mock.
         */
        private fun providesDynamicTableDao(existingTables: List<String> = emptyList()) =
            mockk<DynamicTableDao>(relaxed = true).apply {
                every { tableExists(any()) } answers {
                    val tableName = firstArg<String>()
                    existingTables.contains(tableName)
                }
                every { createTable(any(), any(), any()) } returns true
                every { updateTableSchema(any(), any()) } returns Unit
            }

        /**
         * Provides a DynamicTableDao mock that throws an error.
         */
        private fun providesDynamicTableDaoWithError() = mockk<DynamicTableDao>().apply {
            every { tableExists(any()) } returns false
            every { createTable(any(), any(), any()) } throws RuntimeException("Database error")
        }

        /**
         * Provides a DynamicTableDao mock with table info.
         */
        private fun providesDynamicTableDaoWithTables(tables: List<TableInfo>) = mockk<DynamicTableDao>().apply {
            every { getAllTablesInfo() } returns tables
        }

        /**
         * Provides a DynamicTableDao mock that throws an error on getAllTablesInfo.
         */
        private fun providesDynamicTableDaoWithGetTablesError() = mockk<DynamicTableDao>().apply {
            every { getAllTablesInfo() } throws RuntimeException("Database error")
        }

        /**
         * Provides a DynamicTableDao mock with table data.
         */
        private fun providesDynamicTableDaoWithData(tableName: String, data: List<Map<String, Any?>>) =
            mockk<DynamicTableDao>().apply {
                every { getTableData(tableName) } returns data
            }

        /**
         * Provides a DynamicTableDao mock that throws an error on getTableData.
         */
        private fun providesDynamicTableDaoWithGetDataError() = mockk<DynamicTableDao>().apply {
            every { getTableData(any()) } throws RuntimeException("Database error")
        }

        /**
         * Provides a TableSchemaConverter that converts TableSchemaDto to TableSchema.
         */
        private fun providesTableSchemaConverter() = mockk<Converter<TableSchemaDto, TableSchema>>().apply {
            every { convert(any()) } answers {
                val input = firstArg<TableSchemaDto>()
                TableSchema(
                    tableName = input.tableName,
                    columns = input.columns.map { col ->
                        ColumnDefinition(
                            name = col.name,
                            type = col.type,
                            nullable = col.nullable,
                            primaryKey = col.primaryKey,
                        )
                    },
                )
            }
            every { convertList(any()) } answers {
                val inputList = firstArg<List<TableSchemaDto>>()
                inputList.map { dto ->
                    TableSchema(
                        tableName = dto.tableName,
                        columns = dto.columns.map { col ->
                            ColumnDefinition(
                                name = col.name,
                                type = col.type,
                                nullable = col.nullable,
                                primaryKey = col.primaryKey,
                            )
                        },
                    )
                }
            }
        }
    }

    /**
     * Data class for schema scenarios.
     */
    data class SchemaScenario(
        val tableDtos: List<TableSchemaDto>,
    )

    /**
     * Data class for tables scenarios.
     */
    data class TablesScenario(
        val schemas: List<TableSchema>,
    )

    /**
     * Data class for table info scenarios.
     */
    data class TableInfoScenario(
        val tables: List<TableInfo>,
    )

    /**
     * Data class for table data scenarios.
     */
    data class TableDataScenario(
        val tableName: String,
        val data: List<Map<String, Any?>>,
    )
}
