package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving information about all synchronized tables.
 *
 * This use case retrieves metadata about all tables in the local database,
 * including table names and record counts, typically used for displaying
 * available tables to the user.
 *
 * @param dataSyncRepository Repository for data synchronization operations
 */
class GetTablesUseCase @Inject constructor(
    private val dataSyncRepository: DataSyncRepository,
) {
    /**
     * Executes the table retrieval operation.
     *
     * @return Flow emitting Result with a list of TableInfo objects,
     *         or an error if retrieval fails
     */
    operator fun invoke(): Flow<Result<List<TableInfo>>> =
        dataSyncRepository.getAllTables()
}
