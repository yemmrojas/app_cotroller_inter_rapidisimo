package com.yei.dev.controlerinterrapidisimo.domain.utils

import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus

/**
 * Compares this version string with a remote version using semantic versioning.
 *
 * @param remoteVersion The remote version string to compare against (e.g., "1.3.0")
 * @return VersionComparisonStatus indicating the comparison result
 *
 * Example:
 * ```
 * val status = "1.2.3".compareVersion("1.3.0")
 * // Returns VersionComparisonStatus.UPDATE_NEEDED
 * ```
 */
fun String.compareVersion(remoteVersion: String): VersionComparisonStatus {
    val localParts = this.split(".").mapNotNull { it.toIntOrNull() }
    val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }

    val maxLength = maxOf(localParts.size, remoteParts.size)

    for (i in 0 until maxLength) {
        val localPart = localParts.getOrNull(i) ?: 0
        val remotePart = remoteParts.getOrNull(i) ?: 0

        when {
            localPart < remotePart -> return VersionComparisonStatus.UPDATE_NEEDED
            localPart > remotePart -> return VersionComparisonStatus.AHEAD_OF_SERVER
        }
    }

    return VersionComparisonStatus.UP_TO_DATE
}
