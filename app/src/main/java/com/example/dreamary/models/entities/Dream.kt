package com.example.dreamary.models.entities

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Timestamp
import java.util.Date

data class Dream(
    var id: String = "",
    val title: String = "",
    val content: String = "",
    val dreamType: String = "",
    var lucid: Boolean = false,
    val isShared: Boolean = false,
    val analysis: String = "",
    val emotions: List<String> = emptyList(),
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val sharedWith: Share = Share(emptyList(), emptyList()),
    var audio: Map<String, Any> = mapOf(
        "fileName" to "",
        "duration" to 0,
        "mimeType" to "",
        "path" to "",
        "size" to 0L,
        "url" to "",
        "createdAt" to Date()
    ),

    val characteristics: Map<String, Int> = mapOf(
        "clarity" to 0,
        "emotionalImpact" to 0,
    ),

    var environment: Map<String, Any> = mapOf(
        "dominantColors" to "",
        "season" to "",
        "type" to "",
        "weather" to ""
    ),

    val metadata: Map<String, Any> = mapOf(
        "createdAt" to Date(),
        "updatedAt" to Date(),
        "isDeleted" to false,
        "isFavorite" to false,
        "viewCount" to 0
    ),

    var sleepContext: Map<String, Any> = mapOf(
        "noiseLevel" to "",
        "nbReveils" to "",
        "temperature" to 0,
        "time" to "",
        "quality" to 0,
        "duration" to 0
    ),

    val social: Map<String, Any> = mapOf(
        "likes" to 0,
        "comments" to 0,
        "shares" to 0,
        "similarDreams" to listOf<String>()
    ),

    var tags: Map<String, List<String>> = mapOf(
        "symbols" to listOf(),
        "themes" to listOf(),
        "characters" to listOf(),
        "places" to listOf(),
        "divers" to listOf()
    )
)