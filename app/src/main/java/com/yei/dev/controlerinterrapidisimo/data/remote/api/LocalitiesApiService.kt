package com.yei.dev.controlerinterrapidisimo.data.remote.api

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalitiesResponseDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * API service interface for localities operations.
 *
 * Provides access to the Localities Service endpoint to retrieve
 * available localities (cities/locations) in the system.
 */
interface LocalitiesApiService {
    /**
     * Retrieves the list of localities from the remote service.
     *
     * Endpoint: GET /api/ParametrosFramework/ObtenerLocalidadesRecogidas
     *
     * @return Response containing LocalitiesResponseDto with locality information
     */
    @GET("api/ParametrosFramework/ObtenerLocalidadesRecogidas")
    suspend fun getLocalities(): Response<LocalitiesResponseDto>
}
