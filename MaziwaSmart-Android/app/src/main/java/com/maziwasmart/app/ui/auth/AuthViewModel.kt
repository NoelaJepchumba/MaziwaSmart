
package com.maziwasmart.app.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maziwasmart.app.data.AuthRepository
import com.maziwasmart.app.data.UserProfile
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)

    fun isLoggedIn() = repo.currentUserId != null

    // Email and password login
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            error = "Enter your email and password"
            return
        }

        run(onSuccess) {
            repo.login(email.trim(), password)
        }
    }

    // Google sign-in
    fun signInWithGoogle(
        activity: Activity,
        serverClientId: String,
        onSuccess: () -> Unit
    ) {
        run(onSuccess) {
            repo.signInWithGoogle(activity, serverClientId)
        }
    }

    // Register a new user
    fun register(
        profile: UserProfile,
        password: String,
        onSuccess: () -> Unit
    ) {
        error = when {
            profile.name.isBlank() -> "Enter your full name"
            profile.email.isBlank() -> "Enter your email"
            password.length < 6 -> "Password must be at least 6 characters"
            profile.farmName.isBlank() -> "Enter your farm name"
            else -> null
        }

        if (error != null) return

        run(onSuccess) {
            repo.register(
                profile.copy(email = profile.email.trim()),
                password
            )
        }
    }

    // Reset password
    fun resetPassword(email: String, onResult: (String) -> Unit) {
        if (email.isBlank()) {
            onResult("Type your email first")
            return
        }

        viewModelScope.launch {
            try {
                repo.sendPasswordReset(email.trim())
                onResult("Password reset email sent")
            } catch (e: Exception) {
                onResult(e.message ?: "Could not send reset email")
            }
        }
    }

    // Logout
    fun logout(activity: Activity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                repo.logout(activity)
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout failed", e)
                error = e.message ?: "Could not log out"
            } finally {
                loading = false
            }
        }
    }

    // Shared authentication operation
    private fun run(
        onSuccess: () -> Unit,
        action: suspend () -> Unit
    ) {
        loading = true
        error = null

        viewModelScope.launch {
            try {
                action()
                onSuccess()
            } catch (e: Exception) {
            Log.e("GoogleSignIn", "Authentication failed", e)

            error = "${e::class.simpleName}: ${e.message}"
        } finally {
            loading = false
        }
        }
    }
}
