package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import javax.inject.Inject

/**
 * Converter para transformar TableSchemaDto a TableSchema (modelo de dominio).
 */
class TableSchemaConverter @Inject constructor (
    private val columnDefinitionConverter: Converter<ColumnDefinitionDto, ColumnDefinition>
) : Converter<TableSchemaDto, TableSchema> {
    override fun convert(input: TableSchemaDto): TableSchema = TableSchema(
        tableName = input.tableName,
        columns = columnDefinitionConverter.convertList(input.columns),
        queryCreacion = input.queryCreacion
    )
}
