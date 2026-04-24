package com.yei.dev.controlerinterrapidisimo.data.remote.api

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.SchemaResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * API service interface for database synchronization operations.
 *
 * Provides access to the Data Sync Service endpoint to retrieve
 * database schema definitions for local synchronization.
 */
interface DataSyncApiService {
    /**
     * Retrieves the database schema from the remote service.
     *
     * Endpoint: GET /api/SincronizadorDatos/ObtenerEsquema/true
     *
     * @param usuario Username header for authentication
     * @return Response containing SchemaResponseDto with table definitions
     */
    @GET("api/SincronizadorDatos/ObtenerEsquema/true")
    suspend fun getDatabaseSchema(
        @Header("usuario") usuario: String
    ): Response<SchemaResponseDto>
}
