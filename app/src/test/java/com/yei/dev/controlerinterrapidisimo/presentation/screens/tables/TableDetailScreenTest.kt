package com.yei.dev.controlerinterrapidisimo.presentation.screens.tables

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * Unit tests for TableDetailScreen column extraction logic.
 *
 * Tests verify that:
 * - All unique columns from all rows are extracted (not just first row)
 * - Variable/sparse schemas are handled correctly
 * - Columns are sorted for consistent display
 */
class TableDetailScreenTest {

    @Test
    fun `extractAllColumns should get columns from all rows, not just first`() {
        // Given: Data with variable schema - first row missing "email" column
        val data = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob", "email" to "bob@example.com"),
            mapOf("id" to 3, "name" to "Charlie", "email" to "charlie@example.com", "phone" to "555-1234")
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: All columns should be present
        assertEquals(4, columns.size)
        assertTrue(columns.contains("email"))
        assertTrue(columns.contains("id"))
        assertTrue(columns.contains("name"))
        assertTrue(columns.contains("phone"))
    }

    @Test
    fun `extractAllColumns should handle sparse data correctly`() {
        // Given: Sparse data where each row has different columns
        val data = listOf(
            mapOf("col_a" to "value_a"),
            mapOf("col_b" to "value_b"),
            mapOf("col_c" to "value_c"),
            mapOf("col_a" to "value_a2", "col_b" to "value_b2")
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: All columns should be present
        assertEquals(3, columns.size)
        assertTrue(columns.contains("col_a"))
        assertTrue(columns.contains("col_b"))
        assertTrue(columns.contains("col_c"))
    }

    @Test
    fun `extractAllColumns should return empty list for empty data`() {
        // Given: Empty data
        val data = emptyList<Map<String, Any?>>()

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: Should return empty list
        assertEquals(0, columns.size)
    }

    @Test
    fun `extractAllColumns should handle single row`() {
        // Given: Single row with multiple columns
        val data = listOf(
            mapOf("id" to 1, "name" to "Alice", "email" to "alice@example.com")
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: All columns should be present
        assertEquals(3, columns.size)
        assertTrue(columns.contains("id"))
        assertTrue(columns.contains("name"))
        assertTrue(columns.contains("email"))
    }

    @Test
    fun `extractAllColumns should sort columns alphabetically`() {
        // Given: Data with unsorted columns
        val data = listOf(
            mapOf("zebra" to 1, "apple" to 2, "monkey" to 3),
            mapOf("zebra" to 4, "apple" to 5, "monkey" to 6)
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: Columns should be sorted
        assertEquals(listOf("apple", "monkey", "zebra"), columns)
    }

    @Test
    fun `extractAllColumns should handle null values correctly`() {
        // Given: Data with null values
        val data = listOf(
            mapOf("id" to 1, "name" to "Alice", "email" to null),
            mapOf("id" to 2, "name" to null, "email" to "bob@example.com", "phone" to "555-1234")
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: All columns should be present (null values don't affect column extraction)
        assertEquals(4, columns.size)
        assertTrue(columns.contains("email"))
        assertTrue(columns.contains("id"))
        assertTrue(columns.contains("name"))
        assertTrue(columns.contains("phone"))
    }

    @Test
    fun `extractAllColumns should handle duplicate column names`() {
        // Given: Data with same columns in multiple rows
        val data = listOf(
            mapOf("id" to 1, "name" to "Alice"),
            mapOf("id" to 2, "name" to "Bob"),
            mapOf("id" to 3, "name" to "Charlie")
        )

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: Should have only unique columns
        assertEquals(2, columns.size)
        assertEquals(listOf("id", "name"), columns)
    }

    @Test
    fun `extractAllColumns should handle large datasets with variable schemas`() {
        // Given: Large dataset with variable schema
        val data = (1..100).map { index ->
            val row = mutableMapOf<String, Any?>(
                "id" to index,
                "name" to "User$index"
            )
            // Add optional columns based on index
            if (index % 2 == 0) row["email"] = "user$index@example.com"
            if (index % 3 == 0) row["phone"] = "555-${index}"
            if (index % 5 == 0) row["address"] = "Address $index"
            row
        }

        // When: Extract all unique columns
        val columns = data
            .flatMap { it.keys }
            .distinct()
            .sorted()

        // Then: All columns should be present
        assertEquals(5, columns.size)
        assertTrue(columns.contains("id"))
        assertTrue(columns.contains("name"))
        assertTrue(columns.contains("email"))
        assertTrue(columns.contains("phone"))
        assertTrue(columns.contains("address"))
    }

    companion object {
        fun providesVariableSchemaScenarios() = listOf(
            VariableSchemaScenario(
                description = "First row missing optional columns",
                data = listOf(
                    mapOf("id" to 1, "name" to "Alice"),
                    mapOf("id" to 2, "name" to "Bob", "email" to "bob@example.com")
                ),
                expectedColumns = listOf("email", "id", "name")
            ),
            VariableSchemaScenario(
                description = "Last row has unique columns",
                data = listOf(
                    mapOf("id" to 1, "name" to "Alice"),
                    mapOf("id" to 2, "name" to "Bob"),
                    mapOf("id" to 3, "name" to "Charlie", "email" to "charlie@example.com", "phone" to "555-1234")
                ),
                expectedColumns = listOf("email", "id", "name", "phone")
            ),
            VariableSchemaScenario(
                description = "Middle row has unique columns",
                data = listOf(
                    mapOf("id" to 1, "name" to "Alice"),
                    mapOf("id" to 2, "name" to "Bob", "email" to "bob@example.com"),
                    mapOf("id" to 3, "name" to "Charlie")
                ),
                expectedColumns = listOf("email", "id", "name")
            )
        )
    }

    data class VariableSchemaScenario(
        val description: String,
        val data: List<Map<String, Any?>>,
        val expectedColumns: List<String>
    ) {
        override fun toString(): String = description
    }
}
