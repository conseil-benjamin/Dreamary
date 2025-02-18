package com.example.dreamary.models.entities

import androidx.compose.ui.graphics.Color

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val rarity: String = "",
    val category: String = "",
    val color: String = "",
    val unlockCriteria: Map<String, Any> = emptyMap()
)
