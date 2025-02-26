package com.example.dreamary.models.repositories

import android.content.Context
import android.util.Log
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class SocialRepository(private val context: Context) {
    val db = FirebaseFirestore.getInstance()
    private var _groupsList = MutableStateFlow<List<Group>>(emptyList())
    var groups = _groupsList.asStateFlow()

    private var _usersList = MutableStateFlow<List<User>>(emptyList())
    var users = _usersList.asStateFlow()

    private var _listFriends = MutableStateFlow<List<User>>(emptyList())
    var listFriends = _listFriends.asStateFlow()

    fun getGroupsForCurrentUser(userId: String): StateFlow<List<Group>> {
        try {
            db.collection("group")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val groups = documents.map { document ->
                        val group = document.toObject(Group::class.java)
                        group.copy(
                            id = document.id
                        )
                    }
                    _groupsList.value = groups
                }
            return groups
        } catch (e: Exception) {
            println("Erreur lors de l'ajout du rêve : $e")
            return groups
        }
    }

    fun searchUsers(searchValue: String): StateFlow<List<User>> {
        try {
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
                }
            return users
        }
    catch (e: Exception) {
        println("Erreur lors de la recherche des utilisateurs : $e")
        return users
    }
}

    suspend fun getFriendsForCurrentUser(userId: String): StateFlow<List<User>> {
        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("friends")
                .get()
                .await()

            val friendsUid = snapshot.documents.mapNotNull { if (it.getString("status") == "accepted") it.getString("user2") else null }
            Log.i("friends", friendsUid.toString())

            val friends = friendsUid.map { friendUid ->
                db.collection("users")
                    .document(friendUid)
                    .get()
                    .await()
                    .toObject(User::class.java)
                    ?.copy(id = friendUid)
            }
            Log.i("friends", friends.toString())

            _listFriends.value = friends as List<User>
            return listFriends
        } catch (e: Exception) {
            println("Erreur lors de la récupération des amis : $e")
            return listFriends
        }
    }
}