package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type alias for localities API response.
 * The API returns an array of localities directly: [{...}, {...}]
 */
typealias LocalitiesResponseDto = List<LocalityDto>

/**
 * Data transfer object for locality information.
 * Maps Spanish field names from Interrapidisimo API to domain model.
 */
@Serializable
data class LocalityDto(
    @SerialName("IdLocalidad")
    val localityId: String,
    @SerialName("AbreviacionCiudad")
    val cityAbbreviation: String = "",
    @SerialName("NombreCompleto")
    val fullName: String = ""
)
