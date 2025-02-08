package com.example.cuoi

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileManagement {
    fun isUsernameTaken(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                Log.d("Firestore", "Document Exists: ${document.exists()}, Data: ${document.data}")
                callback(document.exists()) // Returns true if the username exists
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking username", e)
                callback(false)
            }
    }

    private fun getProfileHelper(username: String, callback: (Profile?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("Firestore", "Data: ${document.data}") // Debugging
                    val profile = document.toObject(Profile::class.java)
                    Log.d("Firestore", "Profile: $profile")
                    if (profile == null) {
                        Log.e("Firestore", "Failed to deserialize Profile")
                    }
                    callback(profile)
                } else {
                    Log.e("Firestore", "Document does not exist")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching profile", e)
                callback(null)
            }
    }

    suspend fun getProfile(username: String): Profile? {
        return suspendCoroutine { continuation ->
            getProfileHelper(username) { profile ->
                continuation.resume(profile)
            }
        }
    }

    fun isUsernameExist(username: String): Boolean {
        var exist = false
        isUsernameTaken(username) { callback ->
            if (!callback) {
                exist = true
            }
        }
        return exist
    }

    fun saveProfile(profile: Profile) {
        val username = profile.name
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).set(profile)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile saved successfully for username: $username")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving profile", e)
            }
    }

    fun deleteUsername(username: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(username).delete()
            .addOnSuccessListener {
                Log.d("Firestore", "User profile deleted successfully: $username")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting user profile", e)
            }
    }
}

