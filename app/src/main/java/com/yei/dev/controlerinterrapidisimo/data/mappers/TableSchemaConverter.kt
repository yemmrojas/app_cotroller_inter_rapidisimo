package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema

/**
 * Converter para transformar TableSchemaDto a TableSchema (modelo de dominio).
 */
class TableSchemaConverter(
    private val columnDefinitionConverter: ColumnDefinitionConverter = ColumnDefinitionConverter()
) : Converter<TableSchemaDto, TableSchema> {
    override fun convert(input: TableSchemaDto): TableSchema = TableSchema(
        tableName = input.tableName,
        columns = columnDefinitionConverter.convertList(input.columns)
    )
}
