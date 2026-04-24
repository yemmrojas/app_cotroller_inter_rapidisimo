package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving data from a specific table.
 *
 * This use case retrieves all data from a specified table in the local database,
 * returning it in a flexible format that can handle tables with varying schemas.
 * The data is returned as a list of maps where each map represents a row.
 *
 * Values can be null to properly represent SQL NULL values for nullable columns.
 *
 * @param dataSyncRepository Repository for data synchronization operations
 */
class GetTableDataUseCase @Inject constructor(
    private val dataSyncRepository: DataSyncRepository,
) {
    /**
     * Executes the table data retrieval operation.
     *
     * @param tableName The name of the table to query
     * @return Flow emitting Result with a list of row data maps,
     *         or an error if retrieval fails
     */
    operator fun invoke(tableName: String): Flow<Result<List<Map<String, Any?>>>> =
        dataSyncRepository.getTableData(tableName)
}
