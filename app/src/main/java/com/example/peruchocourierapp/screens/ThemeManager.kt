package com.example.peruchocourierapp.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    private const val PREF = "theme_pref"
    private const val KEY_DARK = "dark_mode"

    val isDarkMode = mutableStateOf(false)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        isDarkMode.value = prefs.getBoolean(KEY_DARK, false)
    }

    fun setDark(context: Context, value: Boolean) {
        isDarkMode.value = value
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK, value)
            .apply()
    }
}