package com.example.dreamary.models.entities

import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

data class User(
    val id : String = "",
    val uid: String = UUID.randomUUID().toString(),
    val email: String = "",
    val username: String = "",
    val fullName: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val tokenFcm: String = "",
    val metadata: Map<String, Any> = mapOf(
        "accountStatus" to "",
        "lastDreamDate" to Date(),
        "isPremium" to false,
        "lastLogin" to Date(),
        "createdAt" to Timestamp.now(),
    ),
    val preferences: Map<String, Any> = mapOf(
        "notifications" to true,
        "theme" to "dark",
        "isPrivateProfile" to false,
        "language" to "fr"
    ),
    val dreamStats: Map<String, Int> = mapOf(
        "nightmares" to 0,
        "totalDreams" to 0,
        "lucidDreams" to 0,
        "longestStreak" to 0,
        "currentStreak" to 0
    ),
    val progression: Map<String, Any> = mapOf(
        "xpNeeded" to 0,
        "level" to 1,
        "xp" to 0,
        "rank" to "",
        "xpGained" to 0
    ),
    val social: Map<String, Any> = mapOf(
        "groups" to listOf<String>(),
        "followers" to 0,
        "following" to 0
    )
)

