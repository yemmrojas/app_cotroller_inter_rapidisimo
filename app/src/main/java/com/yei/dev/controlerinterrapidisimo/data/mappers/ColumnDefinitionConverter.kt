package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.ColumnDefinitionDto
import com.yei.dev.controlerinterrapidisimo.domain.models.ColumnDefinition

/**
 * Converter para transformar ColumnDefinitionDto a ColumnDefinition (modelo de dominio).
 */
class ColumnDefinitionConverter : Converter<ColumnDefinitionDto, ColumnDefinition> {
    override fun convert(input: ColumnDefinitionDto): ColumnDefinition = ColumnDefinition(
        name = input.name,
        type = input.type,
        nullable = input.nullable,
        primaryKey = input.primaryKey
    )
}
