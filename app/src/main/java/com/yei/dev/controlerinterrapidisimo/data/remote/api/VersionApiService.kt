package com.yei.dev.controlerinterrapidisimo.data.remote.api

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.VersionResponseDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * API service interface for version control operations.
 *
 * Provides access to the Version Control Service endpoint to retrieve
 * the current application version for validation purposes.
 */
interface VersionApiService {
    /**
     * Retrieves the current application version from the remote service.
     *
     * Endpoint: GET /api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl
     *
     * @return Response containing VersionResponseDto with the current version
     */
    @GET("api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl")
    suspend fun getCurrentVersion(): Response<VersionResponseDto>
}
