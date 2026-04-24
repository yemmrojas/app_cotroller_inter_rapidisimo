package com.yei.dev.controlerinterrapidisimo.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Home screen - displays user information and navigation options.
 * TODO: Implement full UI in Task 8.3
 */
@Composable
fun HomeScreen(
    onBackClick: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToLocalities: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Home Screen - TODO: Implement in Task 8.3")
    }
}
