package com.yei.dev.controlerinterrapidisimo.domain.repositories

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for database schema synchronization and table data operations.
 *
 * This repository manages the synchronization of database schemas from the remote
 * Data Sync Service to the local database, handles dynamic table creation and updates,
 * and provides access to table information and data.
 *
 * Business Purpose:
 * - Fetches database schema from remote service
 * - Creates and updates tables dynamically based on remote schema definitions
 * - Preserves existing data during schema updates
 * - Provides access to table metadata and data for display purposes
 * - Ensures data integrity through transaction management
 */
interface DataSyncRepository {
    /**
     * Fetches the database schema from the remote Data Sync Service.
     *
     * This method retrieves the schema definitions for all tables that should
     * exist in the local database. The schema includes table names and column
     * definitions with type information.
     *
     * @return Flow emitting Result with a list of TableSchema objects,
     *         or an error if the fetch fails
     */
    fun fetchDatabaseSchema(): Flow<Result<List<TableSchema>>>

    /**
     * Synchronizes tables in the local database based on provided schemas.
     *
     * This method creates new tables and updates existing tables based on the
     * provided schema definitions. Changes are performed within a transaction
     * and rolled back on failure. Existing data is preserved when updating
     * compatible table structures.
     *
     * @param schemas The list of table schemas to synchronize
     * @return Flow emitting Result with SyncResult containing sync statistics,
     *         or an error if synchronization fails
     */
    fun syncTables(schemas: List<TableSchema>): Flow<Result<SyncResult>>

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
     * Values can be null to properly represent SQL NULL values for nullable columns.
     *
     * @param tableName The name of the table to query
     * @return Flow emitting Result with a list of row data maps on success,
     *         or an error if retrieval fails
     */
    fun getTableData(tableName: String): Flow<Result<List<Map<String, Any?>>>>
}
