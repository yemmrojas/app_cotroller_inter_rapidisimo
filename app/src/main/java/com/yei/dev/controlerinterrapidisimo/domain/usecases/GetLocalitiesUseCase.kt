package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.LocalitiesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving localities from the remote service.
 *
 * This use case fetches the list of available localities (cities/locations)
 * from the Localities Service, providing information about service coverage areas.
 *
 * @param localitiesRepository Repository for localities operations
 */
class GetLocalitiesUseCase @Inject constructor(
    private val localitiesRepository: LocalitiesRepository,
) {
    /**
     * Executes the localities retrieval operation.
     *
     * @return Flow emitting Result with a list of Locality objects,
     *         or an error if retrieval fails
     */
    operator fun invoke(): Flow<Result<List<Locality>>> =
        localitiesRepository.fetchLocalities()
}
