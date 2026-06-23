package com.example.peruchocourierapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    private val SESSION_TIMEOUT = 24 * 60 * 60 * 1000L

    fun saveUserSession(
        name: String,
        email: String,
        phone: String,
        role: String,
        dni: String = ""
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("phone", phone)
        editor.putString("role", role)
        editor.putString("dni", dni)
        editor.putLong("last_active_time", System.currentTimeMillis())
        editor.apply()
    }

    fun updateLastActivity() {
        sharedPreferences.edit()
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()
    }

    fun isSessionExpired(): Boolean {
        val lastActive = sharedPreferences.getLong("last_active_time", 0L)

        if (lastActive == 0L) return true

        val now = System.currentTimeMillis()
        return now - lastActive > SESSION_TIMEOUT
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("name", null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("email", null)
    }

    fun getUserPhone(): String? {
        return sharedPreferences.getString("phone", null)
    }

    fun getUserDni(): String? {
        return sharedPreferences.getString("dni", null)
    }

    fun getUserRole(): String? {
        return sharedPreferences.getString("role", null)
    }

    fun saveUserName(newName: String) {
        sharedPreferences.edit()
            .putString("name", newName)
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()
    }

    fun isLoggedIn(): Boolean {
        val email = sharedPreferences.getString("email", null)

        if (email.isNullOrEmpty()) return false

        if (isSessionExpired()) {
            clearSession()
            return false
        }

        return true
    }

    fun clearSession() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}