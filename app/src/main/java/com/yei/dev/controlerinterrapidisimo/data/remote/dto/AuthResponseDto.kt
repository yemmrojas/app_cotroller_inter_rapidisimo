package com.yei.dev.controlerinterrapidisimo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for authentication API response.
 *
 * Note: The API returns many fields as null. Only Usuario is guaranteed to be non-null.
 * Other fields like Identificacion, Nombre, etc. may be null and should be handled gracefully.
 */
@Serializable
data class AuthResponseDto(
    @SerialName("Usuario")
    val username: String,
    @SerialName("Identificacion")
    val identification: String? = null,
    @SerialName("Nombre")
    val name: String? = null,
    @SerialName("Apellido1")
    val apellido1: String? = null,
    @SerialName("Apellido2")
    val apellido2: String? = null,
    @SerialName("Cargo")
    val cargo: String? = null,
    @SerialName("Aplicaciones")
    val aplicaciones: String? = null,
    @SerialName("Ubicaciones")
    val ubicaciones: String? = null,
    @SerialName("MensajeResultado")
    val mensajeResultado: Int? = null,
    @SerialName("IdLocalidad")
    val idLocalidad: String? = null,
    @SerialName("NombreLocalidad")
    val nombreLocalidad: String? = null,
    @SerialName("NomRol")
    val nomRol: String? = null,
    @SerialName("IdRol")
    val idRol: String? = null,
    @SerialName("TokenJWT")
    val tokenJWT: String? = null,
    @SerialName("ModulosApp")
    val modulosApp: String? = null
)
