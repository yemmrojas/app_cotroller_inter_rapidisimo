package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.TablesState
import com.yei.dev.controlerinterrapidisimo.domain.models.TableInfo
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTableDataUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetTablesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the tables screen.
 *
 * Responsible for listing synchronized tables and loading table data.
 *
 * @param getTablesUseCase Use case for retrieving table information
 * @param getTableDataUseCase Use case for retrieving table data
 */
@HiltViewModel
class TablesViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val getTableDataUseCase: GetTableDataUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<TablesState>(TablesState.Loading)
    val state: StateFlow<TablesState> = _state.asStateFlow()

    /**
     * Loads the list of synchronized tables.
     *
     * Requirements: 7.1, 7.2, 7.5, 14.3
     */
    fun loadTables() {
        viewModelScope.launch {
            getTablesUseCase().collect { tablesResult ->
                when (tablesResult) {
                    is Result.Success -> {
                        val tables = tablesResult.data
                        if (tables.isNotEmpty()) {
                            _state.value = TablesState.TablesList(tables)
                        } else {
                            _state.value = TablesState.Error("No tables available")
                        }
                    }
                    is Result.Error -> {
                        _state.value = TablesState.Error(
                            "Failed to load tables: ${tablesResult.error}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Loads data for a specific table.
     *
     * @param tableName The name of the table to load data from
     */
    fun loadTableData(tableName: String) {
        viewModelScope.launch {
            _state.value = TablesState.Loading

            getTableDataUseCase(tableName).collect { tableDataResult ->
                when (tableDataResult) {
                    is Result.Success -> {
                        val tableData = tableDataResult.data
                        if (tableData.isNotEmpty()) {
                            _state.value = TablesState.TableData(tableName, tableData)
                        } else {
                            _state.value = TablesState.Error("Table '$tableName' is empty")
                        }
                    }
                    is Result.Error -> {
                        _state.value = TablesState.Error(
                            "Failed to load table data: ${tableDataResult.error}"
                        )
                    }
                }
            }
        }
    }
}
