package com.yei.dev.controlerinterrapidisimo.presentation.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yei.dev.controlerinterrapidisimo.R
import com.yei.dev.controlerinterrapidisimo.domain.models.LoginState
import com.yei.dev.controlerinterrapidisimo.presentation.components.DialogType
import com.yei.dev.controlerinterrapidisimo.presentation.components.InfoDialog
import com.yei.dev.controlerinterrapidisimo.presentation.viewmodels.LoginViewModel

/**
 * Login screen - user authentication.
 *
 * Provides input fields for username and password, handles authentication,
 * and displays error messages.
 *
 * Requirements: 2.1, 2.4
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Handle navigation on successful login
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess()
        }
    }

    // Derive dialog state directly from ViewModel state
    val showDialog = state is LoginState.Error
    val dialogMessage = when (val currentState = state) {
        is LoginState.Error -> currentState.message
        else -> "Error desconocido"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Interrapidísimo Logo",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "CONTROLLER LOGIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 1.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your credentials",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Usuario input field
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Usuario") },
                singleLine = true,
                enabled = state !is LoginState.Loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.orange),
                    focusedLabelColor = colorResource(R.color.orange),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password input field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                enabled = state !is LoginState.Loading,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (userName.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(userName, password)
                        }
                    },
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password",
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.orange),
                    focusedLabelColor = colorResource(R.color.orange),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login(userName, password)
                },
                enabled = state !is LoginState.Loading &&
                    userName.isNotBlank() &&
                    password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.orange),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                if (state is LoginState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "LOGIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show dialog when there's an error
            if (showDialog) {
                InfoDialog(
                    type = DialogType.ERROR,
                    message = dialogMessage,
                    onDismiss = { viewModel.dismissError() },
                )
            }

            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}
