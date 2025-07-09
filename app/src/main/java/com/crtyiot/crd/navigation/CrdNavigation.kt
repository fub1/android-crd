package com.crtyiot.crd.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.crtyiot.crd.ui.home.HomeScreen

/**
 * Top-level navigation destinations in the CRD app
 */
enum class CrdDestination(val route: String) {
    HOME("home")
}

/**
 * Main navigation component for the CRD app
 * 
 * This composable sets up the navigation graph using Jetpack Navigation Compose,
 * following Google's recommended navigation patterns for modern Android apps.
 */
@Composable
fun CrdNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = CrdDestination.HOME.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(CrdDestination.HOME.route) {
            HomeScreen()
        }
    }
}