package com.yei.dev.controlerinterrapidisimo.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yei.dev.controlerinterrapidisimo.domain.models.HomeState
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.SyncStatus
import com.yei.dev.controlerinterrapidisimo.domain.usecases.GetUserSessionUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.LogoutUseCase
import com.yei.dev.controlerinterrapidisimo.domain.usecases.SyncDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the home screen.
 *
 * Responsible for loading user data, triggering database synchronization,
 * and handling logout functionality.
 *
 * @param getUserSessionUseCase Use case for retrieving user session
 * @param syncDatabaseUseCase Use case for database synchronization
 * @param logoutUseCase Use case for user logout
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserSessionUseCase: GetUserSessionUseCase,
    private val syncDatabaseUseCase: SyncDatabaseUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var currentSyncStatus: SyncStatus = SyncStatus.NOT_SYNCED

    /**
     * Loads user data and initializes the home screen.
     *
     * Requirements: 6.1, 11.5, 14.3
     */
    fun loadUserData() {
        viewModelScope.launch {
            getUserSessionUseCase().collect { sessionResult ->
                when (sessionResult) {
                    is Result.Success -> {
                        val userSession = sessionResult.data
                        if (userSession != null) {
                            // User session exists, update state with success
                            _state.value = HomeState.Success(
                                userSession = userSession,
                                syncStatus = currentSyncStatus,
                            )
                        } else {
                            // No user session, this should not happen on home screen
                            _state.value = HomeState.Error("No user session found")
                        }
                    }

                    is Result.Error -> {
                        _state.value = HomeState.Error(
                            "Failed to load user data: ${sessionResult.error}",
                        )
                    }
                }
            }
        }
    }

    /**
     * Triggers database synchronization.
     */
    fun syncDatabase() {
        viewModelScope.launch {
            // Update sync status to syncing
            currentSyncStatus = SyncStatus.SYNCING
            updateStateWithCurrentSyncStatus()

            syncDatabaseUseCase().collect { syncResult ->
                when (syncResult) {
                    is Result.Success -> {
                        currentSyncStatus = if (syncResult.data.success) {
                            SyncStatus.SYNCED
                        } else {
                            SyncStatus.FAILED
                        }
                        updateStateWithCurrentSyncStatus()
                    }

                    is Result.Error -> {
                        currentSyncStatus = SyncStatus.FAILED
                        updateStateWithCurrentSyncStatus()
                    }
                }
            }
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase().collect { logoutResult ->
                when (logoutResult) {
                    is Result.Success -> {
                        // Logout successful, state will be handled by navigation
                        // The screen should navigate to login
                    }

                    is Result.Error -> {
                        // Logout failed, show error
                        _state.value = HomeState.Error(
                            "Logout failed: ${logoutResult.error}",
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the state with the current sync status.
     */
    private fun updateStateWithCurrentSyncStatus() {
        val currentState = _state.value
        when (currentState) {
            is HomeState.Success -> {
                _state.value = currentState.copy(syncStatus = currentSyncStatus)
            }

            is HomeState.Loading -> {
                // If still loading, sync status update will happen when Success state is set
            }

            is HomeState.Error -> {
                // If in error state, sync status is not relevant
            }
        }
    }
}
