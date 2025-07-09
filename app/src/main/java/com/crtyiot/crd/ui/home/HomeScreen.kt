package com.crtyiot.crd.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.crtyiot.crd.ui.theme.CrdTheme

/**
 * Home screen UI state
 * 
 * Following MVI architecture patterns, this sealed interface represents
 * all possible states of the home screen.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val message: String) : HomeUiState
    data class Error(val errorMessage: String) : HomeUiState
}

/**
 * Home screen composable
 * 
 * This is the main landing screen of the CRD app. It follows the MVI architecture
 * pattern and Material Design 3 guidelines.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Welcome to CRD",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Modern Android App with MVI Architecture",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Built with Jetpack Compose, Hilt, and Clean Architecture",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Preview for HomeScreen
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CrdTheme {
        HomeScreen()
    }
}

/**
 * Preview for HomeScreen in dark theme
 */
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    CrdTheme(darkTheme = true) {
        HomeScreen()
    }
}