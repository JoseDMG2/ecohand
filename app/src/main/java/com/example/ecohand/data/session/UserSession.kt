package com.example.ecohand.data.session

import android.content.Context
import android.content.SharedPreferences

class UserSession(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ecohand_session",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var INSTANCE: UserSession? = null

        fun getInstance(context: Context): UserSession {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSession(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun saveUserSession(userId: Int, username: String, email: String) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}

