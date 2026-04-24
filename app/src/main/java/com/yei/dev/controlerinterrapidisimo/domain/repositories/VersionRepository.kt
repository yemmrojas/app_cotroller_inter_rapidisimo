package com.yei.dev.controlerinterrapidisimo.domain.repositories

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for version control operations.
 *
 * This repository is responsible for retrieving the current version information
 * from the remote Version Control Service, comparing it with the local version,
 * and providing version status for validation.
 *
 * Business Purpose:
 * - Ensures the application version is up-to-date before allowing user access
 * - Provides version comparison for splash screen validation
 * - Enables version mismatch detection and user notification
 */
interface VersionRepository {
    /**
     * Checks the application version against the remote version.
     *
     * This method retrieves the current version from the remote service,
     * compares it with the provided local version, and returns a VersionStatus
     * with the comparison result.
     *
     * @param localVersion The version of the installed application
     * @return Flow emitting Result with VersionStatus on success,
     *         or an error if the request fails
     */
    fun checkVersion(localVersion: String): Flow<Result<VersionStatus>>
}
