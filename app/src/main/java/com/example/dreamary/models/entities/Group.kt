package com.example.dreamary.models.entities

import com.google.firebase.Timestamp

data class Group (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = listOf(),
    val privacy: String = "",
    val owner_id: String = "",
    val created_at: Timestamp = Timestamp.now(),
    val max_members: Int = 0,
    val image_url: String = ""
)

