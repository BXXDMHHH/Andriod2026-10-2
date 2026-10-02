package com.chatflow.app.data

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("chatflow_session", Context.MODE_PRIVATE)
    var token: String? get() = prefs.getString("access_token", null) set(value) { prefs.edit().putString("access_token", value).apply() }
    var username: String? get() = prefs.getString("username", null) set(value) { prefs.edit().putString("username", value).apply() }
    fun clear() { prefs.edit().clear().apply() }
}
