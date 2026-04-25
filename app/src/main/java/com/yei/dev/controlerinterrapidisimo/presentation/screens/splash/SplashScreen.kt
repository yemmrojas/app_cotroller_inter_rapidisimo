package com.yei.dev.controlerinterrapidisimo.presentation.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yei.dev.controlerinterrapidisimo.R
import com.yei.dev.controlerinterrapidisimo.domain.models.SplashState
import com.yei.dev.controlerinterrapidisimo.presentation.components.DialogType
import com.yei.dev.controlerinterrapidisimo.presentation.components.InfoDialog
import com.yei.dev.controlerinterrapidisimo.presentation.viewmodels.SplashViewModel

/**
 * Splash screen - displays app logo and performs version check.
 *
 * Design based on Interrapidísimo branding with:
 * - Truck icon logo
 * - Company name and unit identifier
 * - Version information
 * - System status indicator
 *
 * Requirements: 1.1, 1.4, 1.5, 1.9
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // State for dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(DialogType.ERROR) }
    var dialogMessage by remember { mutableStateOf("") }

    // Trigger version check on first composition
    LaunchedEffect(Unit) {
        viewModel.checkVersionAndSession()
    }

    // Handle navigation and dialog display based on state
    LaunchedEffect(state) {
        when (val currentState = state) {
            is SplashState.NavigateToLogin -> onNavigateToLogin()
            is SplashState.NavigateToHome -> onNavigateToHome()
            is SplashState.VersionMismatch -> {
                dialogType = DialogType.INFO
                dialogMessage = currentState.message
                showDialog = true
            }
            is SplashState.Error -> {
                dialogType = DialogType.ERROR
                dialogMessage = currentState.message
                showDialog = true
            }
            else -> {
                dialogType = DialogType.ERROR
                dialogMessage = "Ocurrio un error desconocido. Por favor, intente nuevamente."
                showDialog = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Spacer to push content up slightly
            Spacer(modifier = Modifier.weight(1f))

            // Logo - Black square with white truck icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Interrapidísimo Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Company name
            Text(
                text = stringResource(R.string.splash_name_inter_rapidisimo),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unit identifier
            Text(
                text = stringResource(R.string.splash_name_controller),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.weight(1.5f))

            // Version and status information at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                // Version info
                Text(
                    text = stringResource(R.string.splash_verifying_system_status).format("v1.0.0"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.LightGray,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status message based on state
                when (state) {
                    is SplashState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = colorResource(R.color.orange),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.splash_syncing_logistic_hub),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    is SplashState.VersionMismatch, is SplashState.Error -> {
                        // Don't show inline message, will show in dialog
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color.Red,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Verificación fallida",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    else -> {
                        // Navigation states - show loading
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = colorResource(R.color.orange),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.splash_syncing_logistic_hub),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loading indicator - only show when loading or navigating
                if (state is SplashState.Loading ||
                    state is SplashState.NavigateToLogin ||
                    state is SplashState.NavigateToHome) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorResource(R.color.orange),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Show dialog when needed
        if (showDialog) {
            InfoDialog(
                type = dialogType,
                message = dialogMessage,
                onDismiss = { showDialog = false }
            )
        }
    }
}
