package com.example.personallifelogger.data

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: AppUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class AppUser(
    val uid: String,
    val email: String?,
    val displayName: String? = null
)