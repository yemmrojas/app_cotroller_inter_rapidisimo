package com.yei.dev.controlerinterrapidisimo.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Sealed class defining all navigation routes in the application.
 * Each route is a serializable data class or object that extends NavKey.
 */
sealed class Routes : NavKey {
    
    /**
     * Splash screen route - initial screen that checks version and session.
     */
    @Serializable
    data object Splash : Routes()
    
    /**
     * Login screen route - authentication screen.
     */
    @Serializable
    data object Login : Routes()
    
    /**
     * Home screen route - main screen after authentication.
     */
    @Serializable
    data object Home : Routes()
    
    /**
     * Tables screen route - displays list of synchronized tables.
     */
    @Serializable
    data object Tables : Routes()
    
    /**
     * Table detail screen route - displays data for a specific table.
     * @param tableName The name of the table to display
     */
    @Serializable
    data class TableDetail(val tableName: String) : Routes()
    
    /**
     * Localities screen route - displays list of localities.
     */
    @Serializable
    data object Localities : Routes()
}
