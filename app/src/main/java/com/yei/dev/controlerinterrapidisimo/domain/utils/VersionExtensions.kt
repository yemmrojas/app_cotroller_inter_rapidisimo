package com.yei.dev.controlerinterrapidisimo.domain.utils

import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus

/**
 * Normalizes a version string by removing dots and converting to an integer.
 * 
 * This approach works for both formats:
 * - Semantic versioning: "1.0.0" -> 100
 * - Simple format: "100" -> 100
 * - Mixed: "2.5.3" -> 253
 *
 * @param version The version string to normalize
 * @return The normalized version as an integer, or 0 if invalid
 */
private fun normalizeVersion(version: String): Int {
    return version.replace(".", "").toIntOrNull() ?: 0
}

/**
 * Compares two version strings by normalizing them (removing dots) and comparing as integers.
 *
 * This simple approach works for the API response format where versions are:
 * - Local: "1.0.0" (semantic versioning)
 * - Remote: "100" (simple number)
 *
 * Both are normalized to integers for comparison:
 * - "1.0.0" -> 100
 * - "100" -> 100
 *
 * @param localVersion The local version string (e.g., "1.0.0")
 * @param remoteVersion The remote version string (e.g., "100")
 * @return VersionComparisonStatus indicating the comparison result
 */
fun compareVersions(localVersion: String, remoteVersion: String): VersionComparisonStatus {
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
