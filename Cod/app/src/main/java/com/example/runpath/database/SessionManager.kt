package com.example.runpath.database

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    // creez un companion object pentru a stoca cheile de acces
    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_DATE_CREATED = "date_created"
    }
    // obtin instanta de shared preferences
    fun getsharedPreferences(): SharedPreferences {
        return sharedPreferences
    }
    // creez o sesiune
    fun createSession(userId: String, username: String, email: String, dateCreated: String) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_DATE_CREATED, dateCreated)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun getDateCreated(): String? {
        return sharedPreferences.getString(KEY_DATE_CREATED, null)
    }

    // sterg sesiunea
    fun clearSession() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}