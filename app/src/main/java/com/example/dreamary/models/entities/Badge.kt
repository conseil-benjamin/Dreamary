package com.example.dreamary.models.entities

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val rarity: String = "",
    val color: String = "#FFFFFF",
    val category: String = "",
    val unlockCriteria: Map<String, Any> = emptyMap(),
    val progression: Long = 0,
    val unlocked: Boolean = false,
    val visibility: Boolean = true,
    val objective : Long = 0,
    val xp : Long = 0
)