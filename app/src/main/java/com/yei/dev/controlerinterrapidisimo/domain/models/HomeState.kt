package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the state of the home screen.
 */
sealed class HomeState {
    /** Home screen is loading user data */
    object Loading : HomeState()

    /** Home screen successfully loaded with user session and sync status */
    data class Success(val userSession: UserSession, val syncStatus: SyncStatus) : HomeState()

    /** Error occurred while loading home screen data */
    data class Error(val message: String) : HomeState()
}