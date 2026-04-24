package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for authentication API response.
 */
@Serializable
data class AuthResponseDto(
    @SerialName("Usuario")
    val username: String,
    @SerialName("Identificacion")
    val identification: String,
    @SerialName("Nombre")
    val name: String
)
