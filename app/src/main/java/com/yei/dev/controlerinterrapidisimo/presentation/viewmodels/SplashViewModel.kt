package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SplashState
import com.yei.dev.controlerinterrapidisimo.domain.models.VersionComparisonStatus
import com.yei.dev.controlerinterrapidisimo.domain.usecases.CheckVersionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetUserSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the splash screen.
 *
 * Responsible for checking application version and restoring user session
 * during app startup.
 *
 * @param checkVersionUseCase Use case for checking application version
 * @param getUserSessionUseCase Use case for retrieving user session
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkVersionUseCase: CheckVersionUseCase,
    private val getUserSessionUseCase: GetUserSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    /**
     * Checks application version and user session to determine navigation.
     *
     * This method performs the following steps:
     * 1. Checks the application version against the API version
     * 2. If version is current, checks for existing user session
     * 3. Navigates to appropriate screen based on results
     *
     * Requirements: 1.1, 1.2, 1.6, 1.7, 1.8, 14.3
     */
    fun checkVersionAndSession() {
        viewModelScope.launch {
            // First check version
            val versionResult = checkVersionUseCase()
            versionResult.collect { versionCheckResult ->
                when (versionCheckResult) {
                    is Result.Success -> {
                        val versionStatus = versionCheckResult.data
                        when (versionStatus.status) {
                            VersionComparisonStatus.UP_TO_DATE -> {
                                // Version is current, check for user session
                                checkUserSession()
                            }

                            VersionComparisonStatus.UPDATE_NEEDED -> {
                                _state.value = SplashState.VersionMismatch(
                                    MESSAGE_UPDATE_NEEDED.format(
                                        versionStatus.localVersion, versionStatus.apiVersion
                                    )
                                )
                            }

                            VersionComparisonStatus.AHEAD_OF_SERVER -> {
                                _state.value = SplashState.VersionMismatch(
                                    MESSAGE_AHEAD_OF_SERVER.format(
                                        versionStatus.localVersion, versionStatus.apiVersion
                                    )
                                )
                            }
                        }
                    }

                    is Result.Error -> {
                        _state.value = SplashState.Error(
                            MESSAGE_VERSION_CHECK_FAILED.format(versionCheckResult.error)
                        )
                    }
                }
            }
        }
    }

    /**
     * Checks for existing user session after version validation.
     */
    private fun checkUserSession() {
        viewModelScope.launch {
            getUserSessionUseCase().collect { sessionResult ->
                when (sessionResult) {
                    is Result.Success -> {
                        val userSession = sessionResult.data
                        if (userSession != null) {
                            // User session exists, navigate to home
                            _state.value = SplashState.NavigateToHome
                        } else {
                            // No user session, navigate to login
                            _state.value = SplashState.NavigateToLogin
                        }
                    }

                    is Result.Error -> {
                        _state.value = SplashState.Error(
                            MESSAGE_VERSION_CHECK_FAILED.format(sessionResult.error)
                        )
                    }
                }
            }
        }
    }

    internal companion object {
        const val MESSAGE_UPDATE_NEEDED = "Update needed: Local version %s is older than API version %s"
        const val MESSAGE_AHEAD_OF_SERVER = "Version ahead: Local version %s is newer than API version %s"
        const val MESSAGE_VERSION_CHECK_FAILED = "Version check failed: %s"
    }
}
