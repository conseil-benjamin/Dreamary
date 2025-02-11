package com.example.dreamary.models.entities

import com.google.firebase.Timestamp

data class Tag(
    val id : String = "",
    val category : String = "",
    val isCustom : Boolean = false,
    val name : String = "",
    val usageCount : Int = 0,
    val userId : String = "",
)

