package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type alias for database schema API response.
 * The API returns an array of table schemas directly: [{...}, {...}]
 */
typealias SchemaResponseDto = List<TableSchemaDto>

/**
 * Data transfer object for table schema definition.
 * Maps Spanish field names from Interrapidisimo API to domain model.
 */
@Serializable
data class TableSchemaDto(
    @SerialName("NombreTabla")
    val tableName: String,
    @SerialName("Columnas")
    val columns: List<ColumnDefinitionDto> = emptyList(),
    @SerialName("QueryCreacion")
    val queryCreacion: String = "",
    @SerialName("Pk")
    val primaryKey: String = "",
    @SerialName("NumeroCampos")
    val numeroCampos: Int = 0,
)

/**
 * Data transfer object for column definition.
 * Maps Spanish field names from Interrapidisimo API to domain model.
 */
@Serializable
data class ColumnDefinitionDto(
    @SerialName("Nombre")
    val name: String,
    @SerialName("Tipo")
    val type: String,
    @SerialName("Nulable")
    val nullable: Boolean = false,
    @SerialName("EsPrimaryKey")
    val primaryKey: Boolean = false
)
