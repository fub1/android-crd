package com.crtyiot.crd

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.crtyiot.crd.ui.theme.CrdTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Test class for MainActivity
 * 
 * This class contains unit tests for the MainActivity and its composables,
 * ensuring they work correctly with the MVI architecture and respond
 * appropriately to different window size classes.
 */
@HiltAndroidTest
class MainActivityTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun crdApp_displaysCorrectly() {
        // Given
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        
        // When
        composeTestRule.setContent {
            CrdTheme {
                CrdApp(windowSizeClass = windowSizeClass)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Welcome to CRD")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("Modern Android App with MVI Architecture")
            .assertExists()
        
        composeTestRule
            .onNodeWithText("Built with Jetpack Compose, Hilt, and Clean Architecture")
            .assertExists()
    }
    
    @Test
    fun crdApp_handlesCompactWindowSize() {
        // Given
        val compactWindowSizeClass = WindowSizeClass.calculateFromSize(DpSize(300.dp, 600.dp))
        
        // When
        composeTestRule.setContent {
            CrdTheme {
                CrdApp(windowSizeClass = compactWindowSizeClass)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Welcome to CRD")
            .assertExists()
    }
    
    @Test
    fun crdApp_handlesExpandedWindowSize() {
        // Given
        val expandedWindowSizeClass = WindowSizeClass.calculateFromSize(DpSize(800.dp, 600.dp))
        
        // When
        composeTestRule.setContent {
            CrdTheme {
                CrdApp(windowSizeClass = expandedWindowSizeClass)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Welcome to CRD")
            .assertExists()
    }
    
    @Test
    fun crdApp_darkTheme_displaysCorrectly() {
        // Given
        val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))
        
        // When
        composeTestRule.setContent {
            CrdTheme(darkTheme = true) {
                CrdApp(windowSizeClass = windowSizeClass)
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Welcome to CRD")
            .assertExists()
    }
}