package com.example.smartassist.settings

import android.content.Context

object UserPreferences {

    private const val PREF_NAME = "smart_assist_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSelectedLanguage(context: Context): String {
        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setSelectedLanguage(
        context: Context,
        languageTag: String
    ) {
        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        prefs.edit()
            .putString(KEY_LANGUAGE, languageTag)
            .apply()
    }
}