package com.example.dreamary.viewmodels.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SocialViewModel : ViewModel() {
    private var _groupsList = MutableStateFlow<List<Group>>(emptyList())
    var groups = _groupsList.asStateFlow()
    val db = FirebaseFirestore.getInstance()
    private var _isLoading = MutableStateFlow(false)
    var isLoading = _isLoading.asStateFlow()

    private var _usersList = MutableStateFlow<List<User>>(emptyList())
    var users = _usersList.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupsForCurrentUser(userId: String): Boolean {
        return try {
            _isLoading.value = true
            db.collection("group")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val groups = documents.map { document ->
                        val group = document.toObject(Group::class.java)
                        group.copy(id = document.id)
                    }.filterNotNull()
                    _groupsList.value = groups
                    Log.i("group", groups.toString())
                    _isLoading.value = false
                }
            true
        } catch (e: Exception) {
            _isLoading.value = false
            println("Erreur lors de l'ajout du rêve : $e")
            false
        }
    }

    fun searchUsers(searchValue: String) {
        if (searchValue.isEmpty()) {
            _usersList.value = emptyList()
            Log.i("search", users.toString())
            return
        }
        Log.i("search", searchValue)
        db.collection("users")
            .orderBy("username")
            .startAt(searchValue)
            .endAt(searchValue + "\uf8ff")
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.map { document ->
                    val users = document.toObject(User::class.java)
                    users.copy(id = document.id)
                }
                _usersList.value = users
                Log.i("usersSearch", users.toString())
                _isLoading.value = false
            }
    }
}