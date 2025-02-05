package com.example.dreamary.viewmodels.Social

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.dreamary.models.entities.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SocialViewModel : ViewModel() {
    private var _groupsList = MutableStateFlow<List<Group>>(emptyList())
    var groups = _groupsList.asStateFlow()
    val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupsForCurrentUser(userId: String): Boolean {
        return try {
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
                }
            true
        } catch (e: Exception) {
            println("Erreur lors de l'ajout du rêve : $e")
            false
        }
    }
}