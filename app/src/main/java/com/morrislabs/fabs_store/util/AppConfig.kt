package com.morrislabs.fabs_store.util

object AppConfig {
    object Api {
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/fabs/app"
        const val STAGING_BASE_URL = "https://fabs-backend.onrender.com/fabs/app"
        const val ADB_REVERSE_LOCALHOST_URL = "http://127.0.0.1:8080/fabs/app"
        const val LOCAL_NETWORK_URL = "http://192.168.100.253:8080/fabs/app"
        const val BASE_URL = ADB_REVERSE_LOCALHOST_URL
    }
}
