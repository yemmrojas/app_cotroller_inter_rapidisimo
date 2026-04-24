package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import com.yei.dev.controlerinterrapidisimo.domain.repositories.VersionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for checking and comparing application version with the remote API version.
 *
 * This use case delegates to the VersionRepository which handles version retrieval
 * and comparison logic.
 *
 * @param versionRepository Repository for version operations
 * @param appVersion The current version of the installed application
 */
class CheckVersionUseCase @Inject constructor(
    private val versionRepository: VersionRepository,
    private val appVersion: String,
) {
    /**
     * Executes the version check operation.
     *
     * @return Flow emitting Result with VersionStatus containing comparison result,
     *         or an error if the version check fails
     */
    operator fun invoke(): Flow<Result<VersionStatus>> =
        versionRepository.checkVersion(appVersion)
}
