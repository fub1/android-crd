package com.crtyiot.crd.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

/**
 * Test module for Hilt dependency injection
 * 
 * This module provides test-specific dependencies that replace production
 * dependencies during testing. It follows the testing best practices for
 * Hilt and modern Android architecture.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class] // Replace with actual production module when created
)
object TestModule {
    
    // Test-specific dependencies will be added here as needed
    // For example: fake repositories, test dispatchers, etc.
    
}