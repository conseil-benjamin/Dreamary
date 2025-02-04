package com.example.dreamary.models.entities

data class GroupMember (
    val group_id : Int,
    val user_id : Int,
    val role : String,
    val created_at : String,
    val joined_at : String
)