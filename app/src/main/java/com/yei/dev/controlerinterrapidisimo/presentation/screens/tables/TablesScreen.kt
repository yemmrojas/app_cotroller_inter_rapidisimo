package com.yei.dev.controlerinterrapidisimo.presentation.screens.tables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Tables screen - displays list of synchronized tables.
 * TODO: Implement full UI in Task 8.4
 */
@Composable
fun TablesScreen(
    onBackClick: () -> Unit,
    onTableSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Tables Screen - TODO: Implement in Task 8.4")
    }
}
