package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the schema definition for a database table.
 *
 * @param tableName The name of the table
 * @param columns The list of column definitions for this table
 */
data class TableSchema(
    val tableName: String,
    val columns: List<ColumnDefinition>
)

/**
 * Represents a column definition in a database table.
 *
 * @param name The name of the column
 * @param type The data type of the column (e.g., TEXT, INTEGER, REAL)
 * @param nullable Whether the column can contain null values
 * @param primaryKey Whether this column is part of the primary key
 */
data class ColumnDefinition(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val primaryKey: Boolean
)

/**
 * Represents information about a table in the database.
 *
 * @param name The name of the table
 * @param recordCount The number of records in the table
 */
data class TableInfo(
    val name: String,
    val recordCount: Int
)
