package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the state of the login screen.
 */
sealed class LoginState {
    /** Login screen is idle, ready for user input */
    object Idle : LoginState()

    /** Login request is in progress */
    object Loading : LoginState()

    /** Login was successful */
    object Success : LoginState()

    /** Login failed with error message */
    data class Error(val message: String) : LoginState()
}