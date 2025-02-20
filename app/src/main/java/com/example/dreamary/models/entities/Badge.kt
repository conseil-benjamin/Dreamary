package com.example.dreamary.models.entities

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val rarity: String = "",
    val category: String = "",
    val color: String = "",
    val unlockCriteria: Map<String, Any> = emptyMap(),
    val acutalProgression: Int = 0,
    val objective: Int = 0,
    val unlocked: Boolean = false,
    val visibility: Boolean = true
)