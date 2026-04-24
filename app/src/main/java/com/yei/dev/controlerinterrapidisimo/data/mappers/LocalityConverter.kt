package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalityDto
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality

/**
 * Converter para transformar LocalityDto a Locality (modelo de dominio).
 */
class LocalityConverter : Converter<LocalityDto, Locality> {
    override fun convert(input: LocalityDto): Locality = Locality(
        cityAbbreviation = input.cityAbbreviation,
        fullName = input.fullName
    )
}
