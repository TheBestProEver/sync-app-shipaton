package com.example

import android.app.Application
import android.util.Log
import com.example.service.RevenueCatManager

class SyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Read API key from local.properties or .env via BuildConfig
        val apiKey = BuildConfig.REVENUECAT_LOCAL_PROPS_KEY.ifBlank {
            BuildConfig.REVENUECAT_API_KEY
        }.trim()

        Log.d("SyncApplication", "Initializing RevenueCat Purchases SDK...")
        RevenueCatManager.init(this, apiKey)
    }
}
