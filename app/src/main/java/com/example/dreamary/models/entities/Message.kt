package com.example.dreamary.models.entities

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val seen : Boolean = false,
    val type : String = "text",
    val dreamId: String = "",
    val dream: Dream? = null
)
