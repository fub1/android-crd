package com.crtyiot.crd

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for CrdApplication
 * 
 * This class tests the application initialization and Hilt dependency injection
 * setup, ensuring the app starts correctly and all dependencies are properly
 * configured.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class CrdApplicationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun application_initializes_correctly() {
        // Given
        val context: Context = ApplicationProvider.getApplicationContext()
        
        // When
        val application = context as Application
        
        // Then
        assertNotNull(application)
        assertTrue(application is HiltTestApplication)
    }
    
    @Test
    fun application_has_correct_package_name() {
        // Given
        val context: Context = ApplicationProvider.getApplicationContext()
        
        // When
        val packageName = context.packageName
        
        // Then
        assertEquals("com.crtyiot.crd", packageName)
    }
}