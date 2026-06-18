package com.example.peruchocourierapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

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
        editor.apply()
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
        val editor = sharedPreferences.edit()
        editor.putString("name", newName)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return !sharedPreferences.getString("email", null).isNullOrEmpty()
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}