package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the state of the tables screen.
 */
sealed class TablesState {
    /** Tables screen is loading data */
    object Loading : TablesState()

    /** Tables list successfully loaded */
    data class TablesList(val tables: List<TableInfo>) : TablesState()

    /** Table data successfully loaded */
    data class TableData(val tableName: String, val data: List<Map<String, Any?>>) : TablesState()

    /** Error occurred while loading tables data */
    data class Error(val message: String) : TablesState()
}