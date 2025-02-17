package com.example.dreamary.models.entities

import androidx.compose.ui.graphics.Color

data class Badge(
    val badgeId: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val rarity: String = "",
    val color: Color = Color.Unspecified,
    val unlockCriteria: Map<String, Any> = emptyMap()
)
