package com.example.csc371_sharebin.data

import android.content.Context

/**
 * Simple SharedPreferences-based session manager.
 * Stores and retrieves the user's name, email, and password
 * so the app can auto-continue without logging in again.
 */
object UserSession {
    private const val PREFS_NAME = "sharebin_user_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD = "user_password"

    // Returns the saved user name, or null if not stored.
    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NAME, null)
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getUserPassword(context: Context): String? {   // NEW
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, null)
    }


    fun saveUser(context: Context, name: String, email: String?, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    // Returns true if both a name and password are saved.
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NAME, null) != null &&
                prefs.getString(KEY_PASSWORD, null) != null
    }

    fun clear(context: Context) {

    }
}
