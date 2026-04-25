package com.yei.dev.controlerinterrapidisimo.presentation.screens.tables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Table detail screen - displays data for a specific table.
 * TODO: Implement full UI in Task 8.5
 */
@Composable
fun TableDetailScreen(
    tableName: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Table Detail Screen: $tableName - TODO: Implement in Task 8.5")
    }
}
