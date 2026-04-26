package com.yei.dev.controlerinterrapidisimo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.yei.dev.controlerinterrapidisimo.presentation.screens.home.HomeScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.localities.LocalitiesScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.login.LoginScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.splash.SplashScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.tables.TableDetailScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.tables.TablesScreen

/**
 * Main navigation wrapper for the application.
 * Uses Navigation 3 with entryProvider and serializable routes.
 *
 * @param modifier Modifier to be applied to the NavDisplay
 */
@Composable
fun NavWrapper(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Routes.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            // Splash Screen - Version check and session restoration
            entry<Routes.Splash> {
                SplashScreen(
                    onNavigateToLogin = {
                        backStack.add(Routes.Login)
                    },
                    onNavigateToHome = {
                        backStack.add(Routes.Home)
                    }
                )
            }

            // Login Screen - User authentication
            entry<Routes.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        // Clear back stack and navigate to home
                        backStack.clear()
                        backStack.add(Routes.Home)
                    }
                )
            }

            // Home Screen - Main screen with user info and navigation options
            entry<Routes.Home> {
                HomeScreen(
                    onNavigateToTables = {
                        backStack.add(Routes.Tables)
                    },
                    onNavigateToLocalities = {
                        backStack.add(Routes.Localities)
                    },
                    onLogout = {
                        // Clear back stack and navigate to login
                        backStack.clear()
                        backStack.add(Routes.Login)
                    }
                )
            }

            // Tables Screen - List of synchronized tables
            entry<Routes.Tables> {
                TablesScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    },
                    onTableSelected = { tableName ->
                        backStack.add(Routes.TableDetail(tableName = tableName))
                    }
                )
            }

            // Table Detail Screen - Display data for a specific table
            entry<Routes.TableDetail> { route ->
                TableDetailScreen(
                    tableName = route.tableName,
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            // Localities Screen - List of localities
            entry<Routes.Localities> {
                LocalitiesScreen(
                    onBackClick = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        },
        modifier = modifier
    )
}
