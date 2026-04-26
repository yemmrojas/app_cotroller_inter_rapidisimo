package com.yei.dev.controlerinterrapidisimo.presentation.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yei.dev.controlerinterrapidisimo.R
import com.yei.dev.controlerinterrapidisimo.domain.models.HomeState
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncStatus
import com.yei.dev.controlerinterrapidisimo.presentation.viewmodels.HomeViewModel

/**
 * Home screen - displays user information and navigation options.
 *
 * Shows authenticated user details, sync status, and provides navigation
 * to Tables and Localities screens.
 *
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToTables: () -> Unit,
    onNavigateToLocalities: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load user data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (val currentState = state) {
            is HomeState.Loading -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.orange),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            is HomeState.Success -> {
                // Success state - show user info and navigation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header with logo and logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = Color.Black,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Logout button
                        IconButton(
                            onClick = {
                                viewModel.logout(onLogout)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Welcome message
                    Text(
                        text = "Welcome",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentState.userSession.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // User info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(R.color.gray_50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            UserInfoRow(label = "Username", value = currentState.userSession.username)
                            Spacer(modifier = Modifier.height(12.dp))
                            UserInfoRow(label = "Name", value = currentState.userSession.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sync status
                    SyncStatusCard(
                        syncStatus = currentState.syncStatus,
                        onSyncClick = { viewModel.syncDatabase() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation buttons
                    Text(
                        text = "NAVIGATION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationButton(
                        text = "Tablas",
                        icon = Icons.Default.TableChart,
                        onClick = onNavigateToTables
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationButton(
                        text = "Localidades",
                        icon = Icons.Default.LocationOn,
                        onClick = onNavigateToLocalities
                    )
                }
            }
            is HomeState.Error -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = currentState.message,
                            fontSize = 16.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadUserData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.orange)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun SyncStatusCard(
    syncStatus: SyncStatus,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (syncStatus) {
                SyncStatus.SYNCED -> Color(0xFFE8F5E9)
                SyncStatus.FAILED -> Color(0xFFFFEBEE)
                SyncStatus.SYNCING -> Color(0xFFFFF3E0)
                SyncStatus.NOT_SYNCED -> colorResource(R.color.gray_50)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Database Sync",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (syncStatus) {
                        SyncStatus.SYNCED -> "Synchronized"
                        SyncStatus.FAILED -> "Failed"
                        SyncStatus.SYNCING -> "Syncing..."
                        SyncStatus.NOT_SYNCED -> "Not synced"
                    },
                    fontSize = 12.sp,
                    color = when (syncStatus) {
                        SyncStatus.SYNCED -> Color(0xFF4CAF50)
                        SyncStatus.FAILED -> Color(0xFFF44336)
                        SyncStatus.SYNCING -> Color(0xFFFF9800)
                        SyncStatus.NOT_SYNCED -> Color.Gray
                    }
                )
            }

            if (syncStatus != SyncStatus.SYNCING) {
                OutlinedButton(
                    onClick = onSyncClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colorResource(R.color.orange)
                    )
                ) {
                    Text("Sync")
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorResource(R.color.orange),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun NavigationButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.orange),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
