package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
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
 * Property-based tests for ColumnDefinitionConverter.
 *
 * Tests the conversion from ColumnDefinitionDto to ColumnDefinition domain model.
 */
class ColumnDefinitionConverterTest {

    @Test
    fun `property - convert should map all fields correctly`() = runTest {
        checkAll(
            iterations = 100,
            providesColumnDefinitionDtoScenarios(),
        ) { dto ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convert(dto)

            // Then
            assert(result.name == dto.name) {
                "Name should match: expected '${dto.name}', got '${result.name}'"
            }
            assert(result.type == dto.type) {
                "Type should match: expected '${dto.type}', got '${result.type}'"
            }
            assert(result.nullable == dto.nullable) {
                "Nullable should match: expected ${dto.nullable}, got ${result.nullable}"
            }
            assert(result.primaryKey == dto.primaryKey) {
                "PrimaryKey should match: expected ${dto.primaryKey}, got ${result.primaryKey}"
            }
        }
    }

    @Test
    fun `property - convertList should convert all items`() = runTest {
        checkAll(
            iterations = 50,
            providesColumnDefinitionDtoListScenarios(),
        ) { dtoList ->
            // Given
            val sut = providesSut()

            // When
            val result = sut.convertList(dtoList)

            // Then
            assert(result.size == dtoList.size) {
                "Result list size should match input: expected ${dtoList.size}, got ${result.size}"
            }
            result.forEachIndexed { index, columnDef ->
                assert(columnDef.name == dtoList[index].name) {
                    "Name should match at index $index"
                }
                assert(columnDef.type == dtoList[index].type) {
                    "Type should match at index $index"
                }
                assert(columnDef.nullable == dtoList[index].nullable) {
                    "Nullable should match at index $index"
                }
                assert(columnDef.primaryKey == dtoList[index].primaryKey) {
                    "PrimaryKey should match at index $index"
                }
            }
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
         * Provides ColumnDefinitionDto scenarios.
         */
        private fun providesColumnDefinitionDtoScenarios(): Arb<ColumnDefinitionDto> = arbitrary {
            val name = Arb.string(minSize = 3, maxSize = 30)
                .filter { it.isNotBlank() && it.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) }
                .bind()

            val type = listOf("TEXT", "INTEGER", "REAL", "BLOB").random()
            val nullable = Arb.boolean().bind()
            val primaryKey = Arb.boolean().bind()

            ColumnDefinitionDto(
                name = name,
                type = type,
                nullable = nullable,
                primaryKey = primaryKey,
            )
        }

        /**
         * Provides ColumnDefinitionDto list scenarios.
         */
        private fun providesColumnDefinitionDtoListScenarios(): Arb<List<ColumnDefinitionDto>> = arbitrary {
            val count = Arb.int(1..10).bind()
            List(count) {
                providesColumnDefinitionDtoScenarios().bind()
            }
        }

        /**
         * Provides the system under test (ColumnDefinitionConverter).
         */
        private fun providesSut(): ColumnDefinitionConverter {
            return ColumnDefinitionConverter()
        }
    }
}
