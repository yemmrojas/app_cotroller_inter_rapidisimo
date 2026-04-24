package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Data transfer object for database schema API response.
 */
@Serializable
data class SchemaResponseDto(
    val tables: List<TableSchemaDto>
)

/**
 * Data transfer object for table schema definition.
 */
@Serializable
data class TableSchemaDto(
    val tableName: String,
    val columns: List<ColumnDefinitionDto>
)

/**
 * Data transfer object for column definition.
 */
@Serializable
data class ColumnDefinitionDto(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val primaryKey: Boolean
)
