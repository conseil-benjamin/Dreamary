package com.example.dreamary.models.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.util.UUID
import com.example.dreamary.R
import com.example.dreamary.models.routes.NavRoutes
import com.google.firebase.auth.userProfileChangeRequest

class AuthRepository(private val context: Context) {
    private val auth = Firebase.auth

    fun createAccountWithEmail(
        context: Context,
        email: String,
        password: String,
        navController: NavController,
        name: String
    ): Flow<AuthResponse> = callbackFlow {
        if (email.isEmpty() || password.isEmpty()) {
            trySend(AuthResponse.Error(message = "Error"))
            return@callbackFlow
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        this.displayName = name
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            trySend(AuthResponse.Success)
                            saveUserData(context, navController, true)
                        } else {
                            trySend(AuthResponse.Error(message = updateTask.exception?.message ?: ""))
                        }
                    }
                } else {
                    trySend(AuthResponse.Error(message = task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    fun signInWithEmail(context: Context, email: String, password: String, navController: NavController): Flow<AuthResponse> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                    saveUserData(context, navController, false)
                } else {
                    trySend(AuthResponse.Error(message = task.exception?.message ?: ""))
                }
            }
        awaitClose()
    }

    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
    }

    fun signInWithGoogle(navController: NavController, newMember: Boolean): Flow<AuthResponse> = callbackFlow {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            Log.i("logGoogle2", credential.toString())
            if (credential is CustomCredential) {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider
                            .getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )

                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    trySend(AuthResponse.Success)
                                    saveUserData(context, navController, newMember)
                                } else {
                                    trySend(AuthResponse.Error(message = it.exception?.message ?: ""))
                                }
                            }

                    } catch (e: Exception) {
                        trySend(AuthResponse.Error(message = e.message ?: ""))
                    }
                }
            } else {
                Log.i("logGoogle", "No credentials available")
                trySend(AuthResponse.Error(message = "No credentials available"))
            }
        } catch (e: Exception) {
            trySend(AuthResponse.Error(message = e.message ?: ""))
            Log.i("logGoogle", e.message ?: "")
        }
        awaitClose()
    }


    @SuppressLint("CommitPrefEdits")
    fun saveUserData (context: Context, navController: NavController, newMember: Boolean){
        val user = auth.currentUser
        user?.let {
            val displayName = it.displayName
            val email = it.email
            val photoUrl = it.photoUrl
            val uid = it.uid

            // Afficher les informations de l'utilisateur
            Log.d("User Info", "Display Name: $displayName")
            Log.d("User Info", "Email: $email")
            Log.d("User Info", "UID: $uid")
        }
        val editor = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit()
        editor.putString("displayName", user?.displayName)
        editor.putString("email", user?.email)
        editor.putString("photoUrl", user?.photoUrl.toString())
        editor.putString("uid", user?.uid)
        editor.apply()

        val editor2 = context.getSharedPreferences("isLoggedIn", Context.MODE_PRIVATE).edit()

        if (newMember) {
            // Si l'utilisateur est un nouveau membre, il doit remplir des informations supplémentaires avant de continuer à utiliser l'application
            // on lui affichera la page info complémentaires tant qu'il ne l'aura pas rempli et qu'il aura isLoggedin à false
            // déclaré un nouveau sharedPreference pour gérer l'état de l'utilisateur
            // par exemple userInCreation = true alors on redirgie vers la page d'info complémentaires
            // si userInCreation = false alors on redirige vers la page d'accueil s'il est login bien sur

            val editor3 = context.getSharedPreferences("userInCreation", Context.MODE_PRIVATE).edit()
            editor3.putBoolean("userInCreation", true)
            editor3.apply()
            editor2.putBoolean("isLoggedIn", false)
            editor2.apply()
            navController.navigate(NavRoutes.UserMoreInformation.route) {
                popUpTo(NavRoutes.Register.route) {inclusive = true}
            }
        } else{
            editor2.putBoolean("isLoggedIn", true)
            editor2.apply()
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.Login.route) {inclusive = true}
            }
        }
    }

}

interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String): AuthResponse
}