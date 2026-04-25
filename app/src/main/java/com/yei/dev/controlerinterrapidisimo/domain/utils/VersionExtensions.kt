package com.yei.dev.controlerinterrapidisimo.domain.utils

import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus

/**
 * Normalizes a version string by removing dots and converting to an integer.
 * 
 * Examples:
 * - "1.0.0" -> 100
 * - "100" -> 100
 * - "2.5.3" -> 253
 * - "1.2" -> 12
 *
 * @param version The version string to normalize
 * @return The normalized version as an integer, or 0 if invalid
 */
private fun normalizeVersion(version: String): Int {
    return version.replace(".", "").toIntOrNull() ?: 0
}

/**
 * Compares two version strings using semantic versioning.
 *
 * Supports both formats:
 * - Semantic versioning: "1.2.3"
 * - Simple format: "100"
 *
 * @param localVersion The local version string (e.g., "1.2.3" or "100")
 * @param remoteVersion The remote version string to compare against (e.g., "1.3.0" or "100")
 * @return VersionComparisonStatus indicating the comparison result
 */
fun compareVersions(localVersion: String, remoteVersion: String): VersionComparisonStatus {
    // First try semantic versioning comparison
    val localParts = localVersion.split(".").mapNotNull { it.toIntOrNull() }
    val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }

    // If both have valid semantic version parts, use semantic comparison
    if (localParts.isNotEmpty() && remoteParts.isNotEmpty()) {
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

    // Fallback: normalize versions by removing dots and compare as integers
    val normalizedLocal = normalizeVersion(localVersion)
    val normalizedRemote = normalizeVersion(remoteVersion)

    return when {
        normalizedLocal < normalizedRemote -> VersionComparisonStatus.UPDATE_NEEDED
        normalizedLocal > normalizedRemote -> VersionComparisonStatus.AHEAD_OF_SERVER
        else -> VersionComparisonStatus.UP_TO_DATE
    }
}

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
    return compareVersions(this, remoteVersion)
}
