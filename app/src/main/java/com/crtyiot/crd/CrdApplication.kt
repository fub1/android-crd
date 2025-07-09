package com.crtyiot.crd

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * CRD Application class
 * 
 * This class serves as the entry point for the application and enables Hilt dependency injection
 * throughout the app. It follows Google's recommended architecture patterns for modern Android apps.
 */
@HiltAndroidApp
class CrdApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}