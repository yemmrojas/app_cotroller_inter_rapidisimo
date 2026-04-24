package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.LocalitiesApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.LocalityDto
import com.yei.dev.controlerinterrapidisimo.domain.models.Locality
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.LocalitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of LocalitiesRepository.
 *
 * Handles fetching locality information from the remote service.
 */
class LocalitiesRepositoryImpl @Inject constructor(
    private val apiService: LocalitiesApiService,
    private val networkHandler: NetworkHandler,
    private val localityConverter: Converter<LocalityDto, Locality>
) : LocalitiesRepository {

    override fun fetchLocalities(): Flow<Result<List<Locality>>> = flow {
        val result = networkHandler.safeApiCall {
            apiService.getLocalities()
        }

        when (result) {
            is Result.Success -> {
                val localities = localityConverter.convertList(result.data.localities)
                emit(Result.Success(localities))
            }
            is Result.Error -> emit(result)
        }
    }
}
