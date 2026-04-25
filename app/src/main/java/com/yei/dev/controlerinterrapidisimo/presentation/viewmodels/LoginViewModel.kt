package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.LoginState
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the login screen.
 *
 * Responsible for handling user authentication and managing login state.
 *
 * @param loginUserUseCase Use case for user authentication
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /**
     * Attempts to authenticate the user with provided credentials.
     *
     * @param userName The username for authentication
     * @param password The user's password
     *
     * Requirements: 2.1, 2.4, 2.6, 14.3
     */
    fun login(userName: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading

            val result = loginUserUseCase(userName, password, "")
            result.collect { loginResult ->
                _state.value = when (loginResult) {
                    is Result.Success -> LoginState.Success
                    is Result.Error -> {
                        val message = getErrorMessage(loginResult.error)
                        LoginState.Error(message)
                    }
                }
            }
        }
    }

    /**
     * Dismisses the current error state and returns to Idle state.
     */
    fun dismissError() {
        if (_state.value is LoginState.Error) {
            _state.value = LoginState.Idle
        }
    }

    private fun getErrorMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> "Network error: ${error.message}"
            is AppError.ApiError -> "Authentication failed: ${error.message}"
            is AppError.ValidationError -> "Invalid input: ${error.message}"
            is AppError.DatabaseError -> "Database error: ${error.message}"
            is AppError.UnknownError -> "Unexpected error: ${error.message}"
        }
    }
}
