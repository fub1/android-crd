package com.crtyiot.crd.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.crtyiot.crd.navigation.CrdDestination

/**
 * Application state holder for the CRD app
 * 
 * This class manages the global state of the app including navigation,
 * window size class, and other app-level state. It follows the state holder
 * pattern recommended by Google for managing complex UI state.
 */
@Stable
class CrdAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass
) {
    
    /**
     * Current navigation destination
     */
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination
    
    /**
     * Whether the app should use navigation rail instead of bottom navigation
     * Based on window size class
     */
    val shouldUseNavigationRail: Boolean
        get() = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    
    /**
     * Whether the app should show navigation drawer
     */
    val shouldShowNavigationDrawer: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    
    /**
     * Navigate to a destination in the app
     * 
     * @param destination The destination to navigate to
     */
    fun navigateToDestination(destination: CrdDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
    
    /**
     * Navigate back in the navigation stack
     */
    fun navigateBack() {
        navController.popBackStack()
    }
    
    /**
     * Check if the current destination is the given destination
     * 
     * @param destination The destination to check
     * @return true if the current destination matches the given destination
     */
    fun isCurrentDestination(destination: CrdDestination): Boolean {
        return currentDestination?.route == destination.route
    }
}

/**
 * Composable function to remember the app state
 * 
 * This follows the state holder pattern and provides a stable reference
 * to the app state across recompositions.
 */
@Composable
fun rememberCrdAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController()
): CrdAppState {
    return remember(navController, windowSizeClass) {
        CrdAppState(
            navController = navController,
            windowSizeClass = windowSizeClass
        )
    }
}