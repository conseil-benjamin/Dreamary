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
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.routes.NavRoutes
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    private val _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()

    private val _friend = MutableStateFlow<String?>(null)
    var friend = _friend.asStateFlow()

    suspend fun verifyIfWeAreFriends(idUser: String, idFriend: String): StateFlow<String> {
        try {
            val userFirebase = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userId = userFirebase.getString("uid", "").toString()
            val snapshot = db.collection("users")
                .document(userId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("user1", userId),
                        Filter.equalTo("user2", idFriend),
                    )
                )
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                 _friend.value = snapshot.documents[0].getString("status")
            } else {
                val snapshot2 = db.collection("users")
                    .document(userId)
                    .collection("friends")
                    .where(
                        Filter.and(
                            Filter.equalTo("user1", idFriend),
                            Filter.equalTo("user2", userId),
                        )
                    )
                    .get()
                    .await()

                if (snapshot2.documents.isNotEmpty()) {
                    Log.i("status", snapshot2.documents[0].getString("status").toString())
                    _friend.value = snapshot2.documents[0].getString("status")
                } else {
                    _friend.value = "notFriend"
                }
            }
        } catch (e: Exception) {
            Log.e("DreamRepository", "Error verifying friendship", e)
        }
        return friend as StateFlow<String>
    }

    fun sendFriendRequest(idUser: String, idFriend: String) {
        db.collection("users")
            .document(idUser)
            .collection("friends")
            .add(
                hashMapOf(
                    "id" to idFriend,
                    "user1" to idUser,
                    "user2" to idFriend,
                    "status" to "pending"
                )
            )
            .addOnSuccessListener {
                Log.i("FriendRequest", "Friend request sent")
            }
            .addOnFailureListener { e ->
                Log.e("FriendRequest", "Error sending friend request", e)
            }

        db.collection("users")
            .document(idFriend)
            .collection("friends")
            .add(
                hashMapOf(
                    "id" to idUser,
                    "user1" to idFriend,
                    "user2" to idUser,
                    "status" to "pending"
                )
            )
            .addOnSuccessListener {
                Log.i("FriendRequest", "Friend request sent")
            }
            .addOnFailureListener { e ->
                Log.e("FriendRequest", "Error sending friend request", e)
            }
    }


    fun createAccountWithEmail(
        context: Context,
        email: String,
        password: String,
        navController: NavController,
        name: String,
        screen: String
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
                            saveUserData(context, navController, true, screen)
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

    fun signInWithEmail(context: Context, email: String, password: String, navController: NavController, screen: String): Flow<AuthResponse> = callbackFlow {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResponse.Success)
                    saveUserData(context, navController, false, screen)
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

    fun checkIfEmailIsAlreadyUsed(email: String, callback: (Boolean) -> Unit): Boolean {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                callback(documents.size() > 0)
            }
            .addOnFailureListener {
                callback(false)
            }
        return false
    }

    fun signInWithGoogle(navController: NavController, screen: String): Flow<AuthResponse> = callbackFlow {
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
                                    checkIfEmailIsAlreadyUsed(email = auth.currentUser?.email ?: "") { isAlreadyUsed ->
                                        Log.i("newMember", isAlreadyUsed.toString())
                                            val newMember = !isAlreadyUsed
                                            Log.i("newMember2", newMember.toString())
                                            trySend(AuthResponse.Success)
                                            saveUserData(context, navController, newMember, screen)
                                        }
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
    fun saveUserData (context: Context, navController: NavController, newMember: Boolean, screen: String){
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
            if (screen == "login"){
                navController.navigate(NavRoutes.UserMoreInformation.route) {
                    popUpTo(NavRoutes.Login.route) {inclusive = true}
                }
            }
            else {
                navController.navigate(NavRoutes.UserMoreInformation.route) {
                    popUpTo(NavRoutes.Register.route) {inclusive = true}
                }
            }
        } else{
            editor2.putBoolean("isLoggedIn", true)
            editor2.apply()
            navController.navigate(NavRoutes.Home.route) {
                popUpTo(NavRoutes.Login.route) {inclusive = true}
            }
        }
    }

    fun getProfileData(idUSer : String): StateFlow<User?> {
        db.collection("users")
            .document(idUSer)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                _userData.value = user
                Log.d("ProfileViewModel", "User data retrieved: $user")
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
        return userData
    }

    fun updateUserStats(userId: String, field: String, value: Int, onSuccess: () -> Unit, onFailure: () -> Unit): StateFlow<User?> {
        val fieldPath = "dreamStats.$field"

        db.collection("users").document(userId)
            .update(fieldPath, value)
            .addOnSuccessListener {
                db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val user = document.toObject(User::class.java)
                            _userData.value = user
                            Log.d("ProfileViewModel", "User data retrieved: $user")
                        } else {
                            Log.d("ProfileViewModel", "User document does not exist")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("ProfileViewModel", "Error retrieving updated user data", e)
                    }
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.w("HomeViewModel", "Error updating document", e)
                onFailure()
            }

        return userData
    }

}


interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String): AuthResponse
}