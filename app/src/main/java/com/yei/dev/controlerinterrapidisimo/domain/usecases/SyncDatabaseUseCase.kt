package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for synchronizing the local database with the remote schema.
 *
 * This use case orchestrates the complete synchronization process:
 * 1. Fetches the database schema from the remote service
 * 2. Synchronizes tables in the local database based on the fetched schema
 *
 * @param dataSyncRepository Repository for data synchronization operations
 */
class SyncDatabaseUseCase @Inject constructor(
    private val dataSyncRepository: DataSyncRepository,
) {
    /**
     * Executes the database synchronization operation.
     *
     * Fetches the schema from the remote service and then synchronizes
     * the local database tables based on the fetched schema.
     *
     * @return Flow emitting Result with SyncResult containing sync statistics,
     *         or an error if synchronization fails
     */
    operator fun invoke(): Flow<Result<SyncResult>> = flow {
        // Step 1: Fetch schema from remote service
        var schemas: Result<List<com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema>>? = null
        dataSyncRepository.fetchDatabaseSchema().collect { result ->
            schemas = result
        }

        // Step 2: If fetch succeeded, sync tables
        when (schemas) {
            is Result.Success -> {
                val schemaList = (schemas as Result.Success).data
                dataSyncRepository.syncTables(schemaList).collect { result ->
                    emit(result)
                }
            }
            is Result.Error -> {
                emit(schemas as Result.Error)
            }
            else -> {
                // Should not happen
                emit(Result.Error(com.yei.dev.controlerinterrapidisimo.domain.models.AppError.UnknownError("Unknown error during schema fetch")))
            }
        }
    }
}
