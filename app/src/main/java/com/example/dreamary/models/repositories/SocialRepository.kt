package com.example.dreamary.models.repositories

import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.Message
import com.example.dreamary.models.entities.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.Filter
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

    private var _messagesList = MutableStateFlow<List<Message>>(emptyList())
    var messages = _messagesList.asStateFlow()

    private var _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    var listConversations = _conversations.asStateFlow()

    private var _friendRequests = MutableStateFlow<List<User>>(emptyList())
    var friendRequests = _friendRequests.asStateFlow()

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
            db.collection("chats")
                .where(
                    Filter.or(
                        Filter.equalTo("user1", userId),
                        Filter.equalTo("user2", userId)
                    )
                )
                .get()
                .addOnSuccessListener { documents ->
                    val conversations = documents.map { document ->
                        val conversation = document.toObject(Conversation::class.java)
                        conversation.copy(
                            id = document.id
                        )
                    }
                    Log.i("conversations", conversations.toString())
                    _conversations.value = conversations
                }
            return listConversations
        } catch (e: Exception) {
            println("Erreur lors de la récupération des conversations : $e")
            return listConversations
        }
    }

    fun updateFriendStatus(userId: String, friendId: String, status: String): StateFlow<List<User>> {
        try {
            val update = hashMapOf<String, Any>(
                "status" to status
            )

            db.collection("users")
                .document(userId)
                .collection("friends")
                .whereEqualTo("receveir", userId)
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(userId)
                            .collection("friends")
                            .document(document.id)
                            .update(update)
                            .addOnSuccessListener {
                                Log.i("update", "Statut de l'ami mis à jour")
                            }
                    }
                }

            db.collection("users")
                .document(friendId)
                .collection("friends")
                .whereEqualTo("sender", friendId)
                .get()
                .addOnSuccessListener { documents ->
                    documents.forEach { document ->
                        db.collection("users")
                            .document(friendId)
                            .collection("friends")
                            .document(document.id)
                            .update(update)
                            .addOnSuccessListener {
                                Log.i("update", "Statut de l'ami mis à jour")
                            }
                    }
                }

            val friendRequests = getFriendRequestsForCurrentUser(userId)
            Log.i("friendRequests", friendRequests.toString())
            _friendRequests.value = friendRequests.value
            return friendRequests
        } catch (e: Exception) {
            println("Erreur lors de la mise à jour du statut de l'ami : $e")
        }
        return friendRequests
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
                .get()
                .addOnSuccessListener { documents ->
                    Log.i("documents", documents.documents.toString())
                    val messages = documents.map { document ->
                        val conversation = document.toObject(Message::class.java)
                        conversation.copy(
                            id = document.id
                        )
                    }
                    Log.i("messages", messages.toString())
                    _messagesList.value = messages
                }
            return messages
        } catch (e: Exception) {
            println("Erreur lors de la récupération des amis : $e")
            return messages
        }
    }

    fun sendMessage(chatId: String, message: Message) {
        val update = hashMapOf<String, Any>(
            "lastMessage" to message.content,
            "lastMessageTimestamp" to message.createdAt,
            "lastSender" to message.senderId
        )
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
                .update(update)
                .addOnSuccessListener {
                    Log.i("miseajour", "Conversation mise à jour")
                }
        } catch (e: Exception) {
            println("Erreur lors de l'envoi du message : $e")
        }
    }

    fun createConversation(conversation: Conversation) {
        try {
            db.collection("chats")
                .document(conversation.chatId)
                .set(conversation)
                .addOnSuccessListener {
                    Log.i("conversation", "Conversation créée")

                }
        } catch (e: Exception) {
            println("Erreur lors de la création de la conversation : $e")
        }
    }
}