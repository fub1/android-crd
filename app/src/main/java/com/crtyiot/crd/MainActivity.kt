package com.crtyiot.crd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.crtyiot.crd.navigation.CrdNavHost
import com.crtyiot.crd.ui.CrdAppState
import com.crtyiot.crd.ui.rememberCrdAppState
import com.crtyiot.crd.ui.theme.CrdTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the CRD app
 * 
 * This activity serves as the entry point for the application and follows Google's
 * recommended architecture patterns for modern Android apps:
 * 
 * - Uses Jetpack Compose for UI
 * - Implements edge-to-edge display with proper insets handling
 * - Integrates with Hilt for dependency injection
 * - Supports responsive design with window size classes
 * - Follows Material Design 3 guidelines
 * - Implements MVI architecture pattern
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            
            CrdTheme {
                CrdApp(
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }
}

/**
 * Main app composable
 * 
 * This composable sets up the main app structure with navigation and responsive design.
 * It follows the single activity pattern recommended by Google and implements the
 * state holder pattern for managing app-level state.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CrdApp(
    windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val appState = rememberCrdAppState(windowSizeClass)
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)
        ) { innerPadding ->
            CrdNavHost(
                navController = appState.navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Preview for CrdApp
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CrdAppPreview() {
    CrdTheme {
        CrdApp(
            windowSizeClass = androidx.compose.material3.windowsizeclass.WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(400.dp, 800.dp)
            )
        )
    }
}