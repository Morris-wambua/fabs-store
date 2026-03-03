package com.morrislabs.fabs_store.util

import com.morrislabs.fabs_store.BuildConfig

object AppConfig {
    object Api {
        val PROD_BASE_URL: String = BuildConfig.PROD_BASE_URL
        val STAGING_BASE_URL: String = BuildConfig.STAGING_BASE_URL

        const val EMULATOR_BASE_URL = "http://10.0.2.2:8080/fabs/app"
        const val ADB_REVERSE_LOCALHOST_URL = "http://127.0.0.1:8080/fabs/app"
        const val LOCAL_NETWORK_URL = "http://192.168.100.253:8080/fabs/app"

        val BASE_URL = if (BuildConfig.DEBUG) ADB_REVERSE_LOCALHOST_URL else ADB_REVERSE_LOCALHOST_URL
    }

    object Media {
        val BUNNY_REFERER: String = BuildConfig.BUNNY_MEDIA_REFERER
    }
}
