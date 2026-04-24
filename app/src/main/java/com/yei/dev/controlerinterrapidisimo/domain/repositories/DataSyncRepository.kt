package com.yei.dev.controlerinterrapidisimo.domain.repositories

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for database schema synchronization and table data operations.
 *
 * This repository manages the synchronization of database schemas from the remote
 * Data Sync Service to the local database, handles dynamic table creation and updates,
 * and provides access to table information and data.
 *
 * Business Purpose:
 * - Synchronizes database schema from remote service to local database
 * - Creates and updates tables dynamically based on remote schema definitions
 * - Preserves existing data during schema updates
 * - Provides access to table metadata and data for display purposes
 * - Ensures data integrity through transaction management
 */
interface DataSyncRepository {
    /**
     * Synchronizes the local database with the remote schema.
     *
     * This method fetches the schema from the remote service, creates or updates
     * tables in the local database, and returns statistics about the operation.
     * Changes are performed within a transaction and rolled back on failure.
     *
     * @return Flow emitting Result with SyncResult containing sync statistics,
     *         or an error if synchronization fails
     */
    fun syncDatabase(): Flow<Result<SyncResult>>

    /**
     * Retrieves information about all tables in the local database.
     *
     * @return Flow emitting Result with a list of TableInfo containing
     *         table names and record counts, or an error if retrieval fails
     */
    fun getAllTables(): Flow<Result<List<TableInfo>>>

    /**
     * Retrieves all data from a specific table.
     *
     * The data is returned as a list of maps where each map represents a row
     * with column names as keys and column values as values. This flexible
     * structure handles tables with varying schemas.
     *
     * @param tableName The name of the table to query
     * @return Flow emitting Result with a list of row data maps on success,
     *         or an error if retrieval fails
     */
    fun getTableData(tableName: String): Flow<Result<List<Map<String, Any>>>>
}
