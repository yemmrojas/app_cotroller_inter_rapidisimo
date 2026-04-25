package com.yei.dev.controlerinterrapidisimo.data.remote.api

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
     * Official URL: https://apitesting.interrapidisimo.co/apicontrollerpruebas/api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl
     *
     * Note: This endpoint returns a simple string (e.g., "100") not a JSON object.
     *
     * @return Response containing the version as a String
     */
    @GET("apicontrollerpruebas/api/ParametrosFramework/ConsultarParametrosFramework/VPStoreAppControl")
    suspend fun getCurrentVersion(): Response<String>
}
