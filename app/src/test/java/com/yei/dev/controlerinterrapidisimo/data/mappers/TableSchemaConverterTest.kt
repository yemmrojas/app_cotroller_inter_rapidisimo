package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Property-based tests for TableSchemaConverter.
 *
 * Tests the conversion from TableSchemaDto to TableSchema domain model.
 * This converter delegates column conversion to ColumnDefinitionConverter.
 */
class TableSchemaConverterTest {

    @Test
    fun `property - convert should map table name and convert columns`() = runTest {
        checkAll(
            iterations = 100,
            providesTableSchemaDtoScenarios(),
        ) { dto ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(dto)

            // Then
            assert(result.tableName == dto.tableName) {
                "Table name should match: expected '${dto.tableName}', got '${result.tableName}'"
            }
            assert(result.columns.size == dto.columns.size) {
                "Columns size should match: expected ${dto.columns.size}, got ${result.columns.size}"
            }
            result.columns.forEachIndexed { index, column ->
                assert(column.name == dto.columns[index].name) {
                    "Column name should match at index $index"
                }
                assert(column.type == dto.columns[index].type) {
                    "Column type should match at index $index"
                }
                assert(column.nullable == dto.columns[index].nullable) {
                    "Column nullable should match at index $index"
                }
                assert(column.primaryKey == dto.columns[index].primaryKey) {
                    "Column primaryKey should match at index $index"
                }
            }
        }
    }

    @Test
    fun `property - convertList should convert all items`() = runTest {
        checkAll(
            iterations = 50,
            providesTableSchemaDtoListScenarios(),
        ) { dtoList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(dtoList)

            // Then
            assert(result.size == dtoList.size) {
                "Result list size should match input: expected ${dtoList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, tableSchema ->
                assert(tableSchema.tableName == dtoList[index].tableName) {
                    "Table name should match at index $index"
                }
                assert(tableSchema.columns.size == dtoList[index].columns.size) {
                    "Columns size should match at index $index"
                }
            }
        }
    }

    @Test
    fun `convert with empty columns list should return schema with empty columns`() {
        // Given
        val sut = providesSut()
        val dto = TableSchemaDto(
            tableName = "empty_table",
            columns = emptyList(),
        )

        // When
        val result = sut.convert(dto)

        // Then
        assert(result.tableName == "empty_table") {
            "Table name should match"
        }
        assert(result.columns.isEmpty()) {
            "Columns should be empty"
        }
    }

    @Test
    fun `convertList with empty list should return empty list`() {
        // Given
        val sut = providesSut()

        // When
        val result = sut.convertList(emptyList())

        // Then
        assert(result.isEmpty()) {
            "Result should be empty list"
        }
    }

    // ========== PROVIDER METHODS ==========

    companion object {
        /**
         * Provides TableSchemaDto scenarios.
         */
        private fun providesTableSchemaDtoScenarios(): Arb<TableSchemaDto> = arbitrary {
            val tableName = Arb.string(minSize = 5, maxSize = 30)
                .filter { it.isNotBlank() && it.matches(Regex("[a-z_][a-z0-9_]*")) }
                .bind()

            val columnCount = Arb.int(1..10).bind()
            val columns = List(columnCount) { index ->
                ColumnDefinitionDto(
                    name = "column_$index",
                    type = listOf("TEXT", "INTEGER", "REAL").random(),
                    nullable = Arb.boolean().bind(),
                    primaryKey = index == 0, // First column is primary key
                )
            }

            TableSchemaDto(
                tableName = tableName,
                columns = columns,
            )
        }

        /**
         * Provides TableSchemaDto list scenarios.
         */
        private fun providesTableSchemaDtoListScenarios(): Arb<List<TableSchemaDto>> = arbitrary {
            val count = Arb.int(1..5).bind()
            List(count) {
                providesTableSchemaDtoScenarios().bind()
            }
        }

        /**
         * Provides the system under test (TableSchemaConverter).
         */
        private fun providesSut(): TableSchemaConverter {
            return TableSchemaConverter()
        }
    }
}
