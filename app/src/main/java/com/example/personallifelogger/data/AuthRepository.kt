package com.example.personallifelogger.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: AppUser?
        get() = auth.currentUser?.let {
            AppUser(
                uid = it.uid,
                email = it.email,
                displayName = it.displayName
            )
        }

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String, displayName: String): Result<AppUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Update display name
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Save user to Firestore
                saveUserToFirestore(user, displayName)

                Result.success(
                    AppUser(
                        uid = user.uid,
                        email = user.email,
                        displayName = displayName
                    )
                )
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<AppUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(
                    AppUser(
                        uid = it.uid,
                        email = it.email,
                        displayName = it.displayName
                    )
                )
            } ?: Result.failure(Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private suspend fun saveUserToFirestore(user: com.google.firebase.auth.FirebaseUser, displayName: String) {
        val userMap = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "displayName" to displayName,
            "createdAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(user.uid).set(userMap).await()
    }
}