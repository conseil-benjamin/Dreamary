package com.example.dreamary.models.entities

import com.google.firebase.Timestamp

data class Conversation (
    val id: String = "",
    val chatId: String = "",
    val user1: User? = User(),
    val user2: User = User(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastSender: String = "",
    val unreadMessagesUser1: Int = 0,
    val unreadMessagesUser2: Int = 0,
    val profilePictureUser1: String = "",
    val profilePictureUser2: String = "",
    val userId1: String = "",
    val userId2: String = ""
)