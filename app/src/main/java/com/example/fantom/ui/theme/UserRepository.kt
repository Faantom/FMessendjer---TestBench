package com.example.fantom

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject

class UserRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    interface UserCallback {
        fun onUserLoaded(user: User?)
        fun onError(e: Exception)
    }

    fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val editor = prefs.edit()
            val json = JSONObject().apply {
                put("uid", user.uid)
                put("displayName", user.displayName)
                put("email", user.email)
            }
            editor.putString(user.uid, json.toString())
            editor.apply()
            onSuccess()
        } catch (e: Exception) {
            Log.e("UserRepository", "saveUser error", e)
            onFailure(e)
        }
    }

    fun loadUser(uid: String, callback: UserCallback) {
        try {
            val jsonString = prefs.getString(uid, null)
            if (jsonString != null) {
                val json = JSONObject(jsonString)
                val user = User(
                    uid = json.getString("uid"),
                    displayName = json.getString("displayName"),
                    email = json.getString("email")
                )
                callback.onUserLoaded(user)
            } else {
                callback.onUserLoaded(null)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "loadUser error", e)
            callback.onError(e)
        }
    }
}