package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the version comparison status between local and API versions.
 *
 * @param localVersion The version of the installed application
 * @param apiVersion The current version provided by the remote service
 * @param status The comparison result between local and API versions
 */
data class VersionStatus(
    val localVersion: String,
    val apiVersion: String,
    val status: VersionComparisonStatus
)

/**
 * Enum representing the possible outcomes of version comparison.
 */
enum class VersionComparisonStatus {
    /** Local version matches the API version */
    UP_TO_DATE,

    /** Local version is older than the API version and needs update */
    UPDATE_NEEDED,

    /** Local version is newer than the API version */
    AHEAD_OF_SERVER
}
