package com.morrislabs.fabs_store.util

import com.google.maps.android.BuildConfig

object AppConfig {
    object Api {
        // Use https for Railway and Render
        const val PROD_BASE_URL = "https://fabs-production.up.railway.app/fabs/app"
        const val STAGING_BASE_URL = "https://fabs-backend.onrender.com/fabs/app"

        // LOCAL TESTING TIPS:
        // 10.0.2.2 is the specific IP for the standard Android Emulator to see your PC's localhost
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/fabs/app"

        // This is perfect for physical device testing on the same WiFi
        const val LOCAL_NETWORK_URL = "http://192.168.100.253:8080/fabs/app"

        // 💡 Pro-Tip: Switch this automatically based on BuildType
        val BASE_URL = if (BuildConfig.DEBUG) LOCAL_NETWORK_URL else LOCAL_NETWORK_URL
    }
}
