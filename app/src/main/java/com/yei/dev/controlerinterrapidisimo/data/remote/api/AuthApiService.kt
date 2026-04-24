package com.yei.dev.controlerinterrapidisimo.data.remote.api

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthRequestDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API service interface for authentication operations.
 *
 * Provides access to the Authentication Service endpoint to validate
 * user credentials and retrieve user information.
 */
interface AuthApiService {
    /**
     * Authenticates a user with the remote service.
     *
     * Endpoint: POST /api/Seguridad/AuthenticaUsuarioApp
     *
     * @param usuario Username header
     * @param identificacion User identification header
     * @param accept Accept header (typically "application/json")
     * @param idUsuario User ID header
     * @param idCentroServicio Service center ID header
     * @param nombreCentroServicio Service center name header
     * @param idAplicativoOrigen Application origin ID header
     * @param contentType Content-Type header (typically "application/json")
     * @param request Authentication request body containing credentials
     * @return Response containing AuthResponseDto with user information
     */
    @POST("api/Seguridad/AuthenticaUsuarioApp")
    suspend fun authenticateUser(
        @Header("Usuario") usuario: String,
        @Header("Identificacion") identificacion: String,
        @Header("Accept") accept: String,
        @Header("IdUsuario") idUsuario: String,
        @Header("IdCentroServicio") idCentroServicio: String,
        @Header("NombreCentroServicio") nombreCentroServicio: String,
        @Header("IdAplicativoOrigen") idAplicativoOrigen: String,
        @Header("Content-Type") contentType: String,
        @Body request: AuthRequestDto
    ): Response<AuthResponseDto>
}
