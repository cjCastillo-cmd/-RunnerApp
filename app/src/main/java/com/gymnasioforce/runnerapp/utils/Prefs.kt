package com.gymnasioforce.runnerapp.utils

import android.content.Context

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("runner_prefs", Context.MODE_PRIVATE)

    var token: String
        get()  = prefs.getString("token", "") ?: ""
        set(v) = prefs.edit().putString("token", v).apply()

    var userId: Int
        get()  = prefs.getInt("user_id", 0)
        set(v) = prefs.edit().putInt("user_id", v).apply()

    var userName: String
        get()  = prefs.getString("user_name", "") ?: ""
        set(v) = prefs.edit().putString("user_name", v).apply()

    fun clear() = prefs.edit().clear().apply()
}
