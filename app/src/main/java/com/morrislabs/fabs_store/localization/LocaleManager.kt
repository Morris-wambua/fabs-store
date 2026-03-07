package com.morrislabs.fabs_store.localization

import android.content.Context
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "locale_preferences"
    private const val KEY_LANGUAGE_TAG_OVERRIDE = "language_tag_override"

    fun getActiveLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val overrideTag = prefs.getString(KEY_LANGUAGE_TAG_OVERRIDE, null)
        if (!overrideTag.isNullOrBlank()) {
            return Locale.forLanguageTag(overrideTag)
        }

        val configLocale = context.resources.configuration.locales
            .takeIf { !it.isEmpty }
            ?.get(0)

        return configLocale ?: Locale.getDefault()
    }

    fun setLanguageOverride(context: Context, languageTag: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (languageTag.isNullOrBlank()) {
                remove(KEY_LANGUAGE_TAG_OVERRIDE)
            } else {
                putString(KEY_LANGUAGE_TAG_OVERRIDE, languageTag)
            }
        }.apply()
    }
}


