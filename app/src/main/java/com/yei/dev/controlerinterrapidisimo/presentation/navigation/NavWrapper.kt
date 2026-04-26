package com.yei.dev.controlerinterrapidisimo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.yei.dev.controlerinterrapidisimo.presentation.screens.home.HomeScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.localities.LocalitiesScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.login.LoginScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.splash.SplashScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.tables.TableDetailScreen
import com.yei.dev.controlerinterrapidisimo.presentation.screens.tables.TablesScreen
import kotlin.reflect.typeOf

/**
 * Main navigation wrapper for the application.
 * Uses Navigation Compose with NavHost and serializable routes.
 *
 * @param modifier Modifier to be applied to the NavHost
 */
@Composable
fun NavWrapper(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier,
        typeMap = mapOf(
            typeOf<String>() to NavType.StringType,
        ),
    ) {
        // Splash Screen - Version check and session restoration
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen - User authentication
        composable<Routes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen - Main screen with user info and navigation options
        composable<Routes.Home> {
            HomeScreen(
                onNavigateToTables = {
                    navController.navigate(Routes.Tables)
                },
                onNavigateToLocalities = {
                    navController.navigate(Routes.Localities)
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }

        // Tables Screen - List of synchronized tables
        composable<Routes.Tables> {
            TablesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTableSelected = { tableName ->
                    navController.navigate(Routes.TableDetail(tableName = tableName))
                }
            )
        }

        // Table Detail Screen - Display data for a specific table
        composable<Routes.TableDetail> { backStackEntry ->
            val route: Routes.TableDetail = backStackEntry.toRoute()
            TableDetailScreen(
                tableName = route.tableName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Localities Screen - List of localities
        composable<Routes.Localities> {
            LocalitiesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
