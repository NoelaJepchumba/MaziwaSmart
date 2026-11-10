
package com.maziwasmart.app.data

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles logging in, registering and logging out with Firebase Authentication.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Email and password login
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    // Register a new farmer
    suspend fun register(profile: UserProfile, password: String) {
        val result = auth.createUserWithEmailAndPassword(
            profile.email,
            password
        ).await()

        val uid = result.user?.uid
            ?: throw Exception("Could not create the account")

        db.collection("users").document(uid).set(profile).await()
    }

    // Send password reset email
    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // Google sign-in
    suspend fun signInWithGoogle(
        activity: Activity,
        serverClientId: String
    ) {
        val credentialManager = CredentialManager.create(activity)

        // Configure Google sign-in
        val googleOption = GetSignInWithGoogleOption.Builder(
            serverClientId
        ).build()

        // Create credential request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        // Request Google credentials
        val result = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = result.credential

        // Check the returned credential
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(
                credential.data
            )

            // Authenticate with Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdToken.idToken,
                null
            )

            auth.signInWithCredential(firebaseCredential).await()

        } else {
            throw Exception("Google sign-in returned an unexpected credential")
        }
    }

    // Logout from Firebase and clear Google credential state
    suspend fun logout(activity: Activity) {
        auth.signOut()

        val credentialManager = CredentialManager.create(activity)

        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}
