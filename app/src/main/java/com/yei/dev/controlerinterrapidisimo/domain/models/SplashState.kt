package com.yei.dev.controlerinterrapidisimo.domain.models

/**
 * Represents the state of the splash screen.
 */
sealed class SplashState {
    /** Splash screen is loading and checking version/session */
    object Loading : SplashState()

    /** Version mismatch detected with error message */
    data class VersionMismatch(val message: String) : SplashState()

    /** Navigation to login screen is required */
    object NavigateToLogin : SplashState()

    /** Navigation to home screen is required */
    object NavigateToHome : SplashState()

    /** Error occurred during splash screen operations */
    data class Error(val message: String) : SplashState()
}