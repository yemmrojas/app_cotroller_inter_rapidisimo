package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.local.AppDatabase
import com.yei.dev.controlerinterrapidisimo.data.local.dao.DynamicTableDao
import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.DataSyncApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.TableSchemaDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncResult
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.models.TableSchema
import com.yei.dev.controlerinterrapidisimo.domain.repositories.DataSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of DataSyncRepository.
 *
 * Handles database schema synchronization from the remote service and provides
 * access to table information and data.
 */
class DataSyncRepositoryImpl @Inject constructor(
    private val apiService: DataSyncApiService,
    private val database: AppDatabase,
    private val dynamicTableDao: DynamicTableDao,
    private val networkHandler: NetworkHandler,
    private val tableSchemaConverter: Converter<TableSchemaDto, TableSchema>,
) : DataSyncRepository {

    override fun fetchDatabaseSchema(): Flow<Result<List<TableSchema>>> = flow {
        val schemaResult = networkHandler.safeApiCall {
            apiService.getDatabaseSchema(DEFAULT_USER)
        }

        when (schemaResult) {
            is Result.Success -> {
                try {
                    val schemas = tableSchemaConverter.convertList(schemaResult.data.tables)
                    emit(Result.Success(schemas))
                } catch (e: Exception) {
                    emit(
                        Result.Error(
                            AppError.DatabaseError(
                                message = MESSAGE_FAILED_CONVERT_SCHEMA,
                                cause = e,
                            ),
                        ),
                    )
                }
            }

            is Result.Error -> {
                emit(
                    Result.Error(
                        AppError.NetworkError(
                            message = MESSAGE_FAILED_FETCH_SCHEMA,
                            cause = null,
                        ),
                    ),
                )
            }
        }
    }

    override fun syncTables(schemas: List<TableSchema>): Flow<Result<SyncResult>> = flow {
        try {
            var tablesCreated = 0
            var tablesUpdated = 0

            // Use transaction for atomic operations
            withContext(Dispatchers.IO) {
                database.runInTransaction {
                    schemas.forEach { schema ->
                        val tableExists = dynamicTableDao.tableExists(schema.tableName)

                        if (tableExists) {
                            // Table exists - this is an update
                            // Preserve existing data by creating a temporary table, copying data,
                            // dropping the old table, and renaming the new table
                            dynamicTableDao.updateTableSchema(schema.tableName, schema.columns)
                            tablesUpdated++
                        } else {
                            // Create new table
                            dynamicTableDao.createTable(schema.tableName, schema.columns)
                            tablesCreated++
                        }
                    }
                }
            }

            emit(
                Result.Success(
                    SyncResult(
                        tablesCreated = tablesCreated,
                        tablesUpdated = tablesUpdated,
                        success = true,
                    ),
                ),
            )
        } catch (e: Exception) {
            emit(
                Result.Error(
                    AppError.DatabaseError(
                        message = MESSAGE_FAILED_SYNC_SCHEMA,
                        cause = e,
                    ),
                ),
            )
        }
    }

    override fun getAllTables(): Flow<Result<List<TableInfo>>> = flow {
        try {
            val tables = withContext(Dispatchers.IO) {
                dynamicTableDao.getAllTablesInfo()
            }
            emit(Result.Success(tables))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    AppError.DatabaseError(
                        message = MESSAGE_FAILED_RETRIEVE_TABLES,
                        cause = e,
                    ),
                ),
            )
        }
    }

    override fun getTableData(tableName: String): Flow<Result<List<Map<String, Any?>>>> = flow {
        try {
            val data = withContext(Dispatchers.IO) {
                dynamicTableDao.getTableData(tableName)
            }
            emit(Result.Success(data))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    AppError.DatabaseError(
                        message = "$MESSAGE_FAILED_RETRIEVE_TABLE_DATA: $tableName",
                        cause = e,
                    ),
                ),
            )
        }
    }

    companion object {
        private const val DEFAULT_USER = "usuario"
        private const val MESSAGE_FAILED_SYNC_SCHEMA = "Failed to synchronize database schema"
        private const val MESSAGE_FAILED_FETCH_SCHEMA = "Failed to fetch database schema from remote service"
        private const val MESSAGE_FAILED_CONVERT_SCHEMA = "Failed to convert schema response"
        private const val MESSAGE_FAILED_RETRIEVE_TABLES = "Failed to retrieve table information"
        private const val MESSAGE_FAILED_RETRIEVE_TABLE_DATA = "Failed to retrieve data from table"
    }
}
