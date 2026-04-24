package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.VersionApiService
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import com.yei.dev.controlerinterrapidisimo.domain.utils.compareVersions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of VersionRepository.
 *
 * Handles version checking by fetching the current version from the remote service
 * and comparing it with the local version.
 */
class VersionRepositoryImpl @Inject constructor(
    private val apiService: VersionApiService,
    private val networkHandler: NetworkHandler
) : VersionRepository {

    override fun checkVersion(localVersion: String): Flow<Result<VersionStatus>> = flow {
        val result = networkHandler.safeApiCall {
            apiService.getCurrentVersion()
        }

        when (result) {
            is Result.Success -> {
                val apiVersion = result.data.version
                val comparisonStatus = compareVersions(localVersion, apiVersion)
                emit(
                    Result.Success(
                        VersionStatus(
                            localVersion = localVersion,
                            apiVersion = apiVersion,
                            status = comparisonStatus
                        )
                    )
                )
            }
            is Result.Error -> emit(result)
        }
    }
}
