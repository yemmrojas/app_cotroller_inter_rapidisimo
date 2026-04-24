package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for authentication request body.
 */
@Serializable
data class AuthRequestDto(
    @SerialName("Mac")
    val mac: String,
    @SerialName("NomAplicacion")
    val nomApplication: String,
    @SerialName("Password")
    val password: String,
    @SerialName("Path")
    val path: String,
    @SerialName("Usuario")
    val user: String
)
