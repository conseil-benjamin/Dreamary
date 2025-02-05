package com.example.dreamary.viewmodels.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.dreamary.models.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()

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
}