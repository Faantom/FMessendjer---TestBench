package com.example.fantom

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    interface UserCallback {
        fun onUserLoaded(user: User?)
        fun onError(e: Exception)
    }

    fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "displayName" to user.displayName,
            "email" to user.email,
            "avatarUrl" to user.avatarUrl,
            "status" to user.status
        )

        usersCollection.document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "saveUser error", e)
                onFailure(e)
            }
    }

    fun loadUser(uid: String, callback: UserCallback) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback.onUserLoaded(user)
                } else {
                    callback.onUserLoaded(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "loadUser error", e)
                callback.onError(e)
            }
    }
}