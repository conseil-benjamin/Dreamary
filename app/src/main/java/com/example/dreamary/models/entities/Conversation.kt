package com.example.dreamary.models.entities

import com.google.firebase.Timestamp

data class Conversation (
    val id : String = "",
    val chatId : String = "",
    val user1: String = "",
    val user2: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastSender: String = "",
    val unreadMessagesUser1: Int = 0,
    val unreadMessagesUser2: Int = 0,
    val profilePictureUser1: String = "",
    val profilePictureUser2: String = "",
)