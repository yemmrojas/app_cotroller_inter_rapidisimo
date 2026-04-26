package com.yei.dev.controlerinterrapidisimo.presentation.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yei.dev.controlerinterrapidisimo.R
import com.yei.dev.controlerinterrapidisimo.domain.models.TablesState
import com.yei.dev.controlerinterrapidisimo.presentation.components.LoadingController
import com.yei.dev.controlerinterrapidisimo.presentation.components.ToolbarController
import com.yei.dev.controlerinterrapidisimo.presentation.viewmodels.TablesViewModel

/**
 * Table detail screen - displays data for a specific table.
 *
 * Shows table data in a scrollable format with variable column structures.
 *
 * Requirements: 7.3, 7.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    tableName: String,
    viewModel: TablesViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load table data on first composition
    LaunchedEffect(tableName) {
        viewModel.loadTableData(tableName)
    }

    Scaffold(
        topBar = {
            ToolbarController(
                onBackClick = onBackClick,
                text = tableName,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
        ) {
            when (val currentState = state) {
                is TablesState.Loading -> {
                    // Loading state
                    LoadingController()
                }

                is TablesState.TableData -> {
                    // Success state - show table data
                    if (currentState.data.isEmpty()) {
                        // Empty state
                        TableEmptyState()
                    } else {
                        // Table data display
                        TableDataView(
                            data = currentState.data,
                        )
                    }
                }

                is TablesState.TablesList -> {
                    // This state is handled by TablesScreen
                    // Should not reach here in TableDetailScreen
                }

                is TablesState.Error -> {
                    // Error state
                    ErrorState(
                        message = currentState.message,
                        viewModel = viewModel,
                        tableName = tableName,
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    viewModel: TablesViewModel,
    tableName: String = "",
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.loadTableData(tableName) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.orange),
                ),
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun TableEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = "Empty table",
                tint = Color.Gray,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Table is empty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No data available in this table",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TableDataView(
    data: List<Map<String, Any?>>,
) {
    // Get all unique column names from all rows to handle variable/sparse schemas
    val columns = data
        .flatMap { it.keys }
        .distinct()
        .sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Table info header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(R.color.gray_50),
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Records: ${data.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
                Text(
                    text = "Columns: ${columns.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable table
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.orange).copy(alpha = 0.1f),
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        columns.forEach { column ->
                            Text(
                                text = column,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.width(120.dp),
                            )
                        }
                    }
                }
            }

            // Data rows
            itemsIndexed(data) { index, row ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index % 2 == 0)
                            Color.White
                        else
                            colorResource(R.color.gray_50),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        columns.forEach { column ->
                            val value = row[column]
                            Text(
                                text = value?.toString() ?: "null",
                                fontSize = 14.sp,
                                color = if (value == null) Color.Gray else Color.Black,
                                modifier = Modifier.width(120.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
