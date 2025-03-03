package com.example.dreamary.models.entities

data class Share(
    val users: List<User> = emptyList(),
    val groups: List<Group> = emptyList(),
)
