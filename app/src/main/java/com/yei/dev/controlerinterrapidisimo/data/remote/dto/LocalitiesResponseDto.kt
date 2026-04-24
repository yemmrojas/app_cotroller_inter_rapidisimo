package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for localities API response.
 */
@Serializable
data class LocalitiesResponseDto(
    val localities: List<LocalityDto>
)

/**
 * Data transfer object for locality information.
 */
@Serializable
data class LocalityDto(
    @SerialName("AbreviacionCiudad")
    val cityAbbreviation: String,
    @SerialName("NombreCompleto")
    val fullName: String
)
