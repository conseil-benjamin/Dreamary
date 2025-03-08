package com.example.dreamary.models.repositories

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.User
import com.example.dreamary.utils.SnackbarManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SocialRepository(private val context: Context) {
    val db = FirebaseFirestore.getInstance()
    private var _groupsList = MutableStateFlow<List<Group>>(emptyList())
    var groups = _groupsList.asStateFlow()

    private var _usersList = MutableStateFlow<List<User>>(emptyList())
    var users = _usersList.asStateFlow()

    private var _listFriends = MutableStateFlow<List<User>>(emptyList())
    var listFriends = _listFriends.asStateFlow()

    private var _messagesList = MutableStateFlow<List<Message>>(emptyList())
    var messages = _messagesList.asStateFlow()

    private var _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    var listConversations = _conversations.asStateFlow()

    private var _friendRequests = MutableStateFlow<List<User>>(emptyList())
    var friendRequests = _friendRequests.asStateFlow()

    private var _userData = MutableStateFlow<User?>(null)
    var userData = _userData.asStateFlow()

    private val newConversationFlow = MutableStateFlow<List<Conversation>>(emptyList())

    private var _share = MutableStateFlow<Share?>(null)
    var share = _share.asStateFlow()

    fun getProfileData(idUSer : String): StateFlow<User> {
        try {
            db.collection("users")
                .document(idUSer)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    _userData.value = user
                }
            return userData as StateFlow<User>
        } catch (e: Exception) {
            println("Erreur lors de la récupération des données de l'utilisateur : $e")
            return userData as StateFlow<User>
        }
    }

    suspend fun updateUnreadMessages(chatId: String, userId: String) {
        try {
            val update = hashMapOf<String, Any>(
                "unreadMessagesUser1" to 0,
                "unreadMessagesUser2" to 0
            )

            db.collection("chats")
                .document(chatId)
                .get()
                .await()
                .toObject(Conversation::class.java)
                ?.let { conversation ->
                    if (conversation.userId1 == userId) {
                        update["unreadMessagesUser1"] = 0
                    } else {
                        update["unreadMessagesUser2"] = 0
                    }
                }

            db.collection("chats")
                .document(chatId)
                .update(update)
                .addOnSuccessListener {
                    Log.i("update", "Messages lus")
                }
        } catch (e: Exception) {
            println("Erreur lors de la mise à jour des messages non lus : $e")
        }
    }

    suspend fun getGroupsForCurrentUser(userId: String): StateFlow<List<Group>> {
        try {
            Log.i("userIdGr", userId)
            db.collection("group")
                .whereArrayContains("members", userId)
                .get()
                .await()
                .documents.mapNotNull { document ->
                    val group = document.toObject(Group::class.java)
                    group?.copy(id = document.id)
                }
                .let {
                    _groupsList.value = it
                    Log.i("groups", it.toString())
                }

            Log.i("groupsGr", groups.toString())
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

            val friendsUid = snapshot.documents.mapNotNull { if (it.getString("status") == "accepted" && it.getString("receveir") == userId) it.getString("sender") else if (it.getString("status") == "accepted" && it.getString("sender") == userId) it.getString("receveir") else null }
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

    fun getConversationsForCurrentUser(userId: String): StateFlow<List<Conversation>> {
        try {
            Log.i("userId", userId)
            val list = mutableListOf<Conversation>()

            val query = db.collection("chats")
                .where(Filter.or(
                    Filter.equalTo("userId1", userId),
                    Filter.equalTo("userId2", userId)
                ))
                .get()

            query.addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val conversation = document.toObject(Conversation::class.java)
                    list.add(conversation.copy(id = document.id))
                }

                // Tri manuel
                list.sortByDescending { it.lastMessageTimestamp }
                Log.i("conversations", list.toString())
                _conversations.value = list
            }

            return listConversations
        } catch (e: Exception) {
            println("Erreur lors de la récupération des conversations : $e")
            return listConversations
        }
    }

    suspend fun deleteFriend(userId: String, friendId: String): StateFlow<List<User>> {
        try {
            db.collection("users")
                .document(userId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("status", "accepted"),
                        Filter.equalTo("sender", friendId),
                        Filter.equalTo("receveir", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(userId)
                            .collection("friends")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.i("delete", "Ami supprimé")
                            }
                    }
                }

            db.collection("users")
                .document(friendId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("status", "accepted"),
                        Filter.equalTo("sender", friendId),
                        Filter.equalTo("receveir", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(friendId)
                            .collection("friends")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.i("delete", "Ami supprimé")
                            }
                    }
                }

            db.collection("chats")
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("userId1", userId),
                            Filter.equalTo("userId2", friendId)
                        ),
                        Filter.and(
                            Filter.equalTo("userId1", friendId),
                            Filter.equalTo("userId2", userId)
                        )
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("chats")
                            .document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.i("delete", "Conversation supprimée")
                            }
                    }
                }

            val friends = getFriendsForCurrentUser(userId)
            _listFriends.value = friends.value
            Log.i("delete", friends.toString())
            SnackbarManager.showMessage("Ami supprimé avec succès !", R.drawable.success)
            return listFriends
        } catch (e: Exception) {
            println("Erreur lors de la suppression de l'ami : $e")
        }
        return listFriends
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFriendStatus(userId: String, friendId: String, status: String): StateFlow<List<User>> {
        try {
            val update = hashMapOf<String, Any>(
                "status" to status
            )

            val tasks = mutableListOf<Task<Void>>()

            // Update côté receiver
            db.collection("users")
                .document(userId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("status", "pending"),
                        Filter.equalTo("sender", friendId),
                        Filter.equalTo("receveir", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        val task = db.collection("users")
                            .document(userId)
                            .collection("friends")
                            .document(document.id)
                            .update(update)
                            .addOnSuccessListener {
                                Log.i("update", "Statut receiver mis à jour")
                            }
                        tasks.add(task)
                    }
                }

            // Update côté sender
            db.collection("users")
                .document(friendId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("status", "pending"),
                        Filter.equalTo("sender", friendId),
                        Filter.equalTo("receveir", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        val task = db.collection("users")
                            .document(friendId)
                            .collection("friends")
                            .document(document.id)
                            .update(update)
                            .addOnSuccessListener {
                                Log.i("update", "Statut sender mis à jour")
                            }
                        tasks.add(task)
                    }

                    // Attendre que toutes les requêtes soient terminées
                    Tasks.whenAllComplete(tasks)
                        .addOnSuccessListener {
                            Log.i("update", "Toutes les mises à jour terminées")

                            // Récupération des demandes à jour
                            val friendRequests = getFriendRequestsForCurrentUser(userId)
                            _friendRequests.value = friendRequests.value
                        }
                        .addOnFailureListener {
                            Log.e("update", "Erreur lors des mises à jour")
                        }
                }
            return friendRequests

        } catch (e: Exception) {
            Log.e("update", "Erreur lors de la mise à jour : $e")
            return friendRequests
        }
    }

    fun getFriendRequestsForCurrentUser(userId: String): StateFlow<List<User>> {
        try {
            Log.i("userId", userId)
            db.collection("users")
                .document(userId)
                .collection("friends")
                .where(
                    Filter.and(
                        Filter.equalTo("status", "pending"),
                        Filter.equalTo("receveir", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    Log.i("documents", documents.documents.toString())
                    val tasks = documents.mapNotNull { document ->
                        val friendUid = document.getString("sender")
                        friendUid?.let {
                            db.collection("users")
                                .document(it)
                                .get()
                                .continueWith { task ->
                                    val user = task.result.toObject(User::class.java)
                                    user?.copy(id = it)
                                }
                        }
                    }

                    Tasks.whenAllSuccess<User>(tasks)
                        .addOnSuccessListener { users ->
                            Log.i("friendRequests", users.toString())
                            _friendRequests.value = users
                        }
                }
            return friendRequests
        } catch (e: Exception) {
            println("Erreur lors de la récupération des demandes d'amis : $e")
            return friendRequests
        }
    }

    fun getMessagesForCurrentUser(chatId: String): StateFlow<List<Message>> {
        try {
            Log.i("chatId", chatId)
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt")
                .addSnapshotListener {
                    documents, e ->
                    if (e != null) {
                        Log.w("messages", "Erreur lors de la récupération des messages", e)
                        return@addSnapshotListener
                    }

                    val messages = documents?.map { document ->
                        val message = document.toObject(Message::class.java)
                        message.copy(id = document.id)
                    } ?: emptyList()
                    _messagesList.value = messages

                    Log.i("messages", messages.toString())
                }
            return messages
        } catch (e: Exception) {
            println("Erreur lors de la récupération des amis : $e")
            return messages
        }
    }

    suspend fun sendMessage(chatId: String, message: Message) {
        // todo: faire en sorte qu'on puisse mettre à jour correctement le nombre de messages
        // todo: non lu pour chaque utilisateur
        val update = hashMapOf<String, Any>(
            "lastMessage" to message.content,
            "lastMessageTimestamp" to message.createdAt,
            "lastSender" to message.senderId,
            "unreadMessagesUser1" to 0,
            "unreadMessagesUser2" to 0
        )
        Log.i("chatIdSendMessage", chatId)
        Log.i("chatIdSendMessage", message.toString())
        try {
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    Log.i("message", "Message envoyé")
                }

            db.collection("chats")
                .document(chatId)
                .get()
                .await()
                .toObject(Conversation::class.java)
                ?.let { conversation ->
                    if (conversation.userId1 == message.senderId) {
                        update["unreadMessagesUser1"] = conversation.unreadMessagesUser1
                        update["unreadMessagesUser2"] = conversation.unreadMessagesUser2 + 1
                    } else {
                        update["unreadMessagesUser1"] = conversation.unreadMessagesUser1 + 1
                        update["unreadMessagesUser2"] = conversation.unreadMessagesUser2
                    }
                }

            db.collection("chats")
                .document(chatId)
                .update(update)
                .addOnSuccessListener {
                    Log.i("miseajour", "Conversation mise à jour")
                }
        } catch (e: Exception) {
            println("Erreur lors de l'envoi du message : $e")
        }
    }

    suspend fun createConversation(conversation: Conversation): StateFlow<List<Conversation>> {
        try {
            Log.i("conversation", conversation.toString())

            val querySnapshot = db.collection("chats")
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("userId1", conversation.userId1),
                            Filter.equalTo("userId2", conversation.userId2)
                        ),
                        Filter.and(
                            Filter.equalTo("userId1", conversation.userId2),
                            Filter.equalTo("userId2", conversation.userId1)
                        )
                    ),
                )
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.i("conversation", "Conversation inexistante")
                // Si la conversation n'existe pas, on la crée
                val uuid = UUID.randomUUID().toString()
                conversation.chatId = uuid

                db.collection("chats")
                    .document(uuid)
                    .set(conversation)
                    .await()

                Log.i("conversation", "Conversation créée")

                // Récupérer la conversation après création
                val documentSnapshot = db.collection("chats").document(uuid).get().await()
                val newConversation = documentSnapshot.toObject(Conversation::class.java)
                Log.i("newConversation", newConversation.toString())
                newConversation?.let {
                    newConversationFlow.value = listOf(it)
                }
            } else {
                Log.i("conversation", "Conversation existante")
                // Si la conversation existe déjà, on la charge depuis Firestore
                val documentSnapshot = querySnapshot.documents[0]
                val existingConversation = documentSnapshot.toObject(Conversation::class.java)
                Log.i("existingConversation", existingConversation.toString())
                existingConversation?.let {
                    newConversationFlow.value = listOf(it)
                }
            }

        } catch (e: Exception) {
            Log.e("conversation", "Erreur : $e")
        }

        return newConversationFlow.asStateFlow()
    }


    suspend fun getFriendsAndGroupForCurrentUser(userId: String): StateFlow<Share> {
        try {
            val friends = getFriendsForCurrentUser(userId)
            val groups = getGroupsForCurrentUser(userId)

            Log.i("friends", friends.value.toString())
            Log.i("groups", groups.value.toString())

            _share.value = Share(friends.value, groups.value)

            return share as StateFlow<Share>
        } catch (e: Exception) {
            println("Erreur lors de la récupération des groupes : $e")
        }
        return share as StateFlow<Share>
    }
}