package com.yei.dev.controlerinterrapidisimo.domain.repositories

import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for locality data operations.
 *
 * This repository is responsible for retrieving locality information from the
 * remote Localities Service, providing access to service coverage areas and
 * location data.
 *
 * Business Purpose:
 * - Provides access to available localities (cities/locations) in the system
 * - Enables users to view service coverage areas
 * - Supports location-based features and filtering
 */
interface LocalitiesRepository {
    /**
     * Fetches the list of localities from the remote Localities Service.
     *
     * @return Flow emitting Result with a list of Locality objects on success,
     *         or an error if the request fails
     */
    fun fetchLocalities(): Flow<Result<List<Locality>>>
}
