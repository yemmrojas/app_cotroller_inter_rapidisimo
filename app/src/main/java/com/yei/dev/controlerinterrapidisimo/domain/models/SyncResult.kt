package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the result of a database synchronization operation.
 *
 * @param tablesCreated The number of tables created during synchronization
 * @param tablesUpdated The number of tables updated during synchronization
 * @param success Whether the synchronization completed successfully
 */
data class SyncResult(
    val tablesCreated: Int,
    val tablesUpdated: Int,
    val success: Boolean
)

/**
 * Enum representing the current status of database synchronization.
 */
enum class SyncStatus {
    /** Database has not been synchronized yet */
    NOT_SYNCED,

    /** Synchronization is currently in progress */
    SYNCING,

    /** Database has been successfully synchronized */
    SYNCED,

    /** Synchronization failed */
    FAILED
}
