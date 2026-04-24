package com.yei.dev.controlerinterrapidisimo.domain.usecases

import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for synchronizing the local database with the remote schema.
 *
 * This use case delegates to the DataSyncRepository which handles the complete
 * synchronization process including schema fetching and table creation/updates.
 *
 * @param dataSyncRepository Repository for data synchronization operations
 */
class SyncDatabaseUseCase @Inject constructor(
    private val dataSyncRepository: DataSyncRepository,
) {
    /**
     * Executes the database synchronization operation.
     *
     * @return Flow emitting Result with SyncResult containing sync statistics,
     *         or an error if synchronization fails
     */
    operator fun invoke(): Flow<Result<SyncResult>> =
        dataSyncRepository.syncDatabase()
}
