package com.morrislabs.fabs_store.util

import com.morrislabs.fabs_store.BuildConfig

object SecretsConfig {

    object Firebase {
        val PROJECT_ID: String = BuildConfig.FIREBASE_PROJECT_ID
        const val FCM_SERVER_KEY = ""
        const val SERVICE_ACCOUNT_JSON_PATH = ""
    }

}
